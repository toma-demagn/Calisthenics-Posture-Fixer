import math

import cv2
import mediapipe as mp
import numpy as np


def compute_circle_edge_strength(edge_image, center, radius, thickness=5):
    mask = np.zeros(edge_image.shape, dtype=np.uint8)
    cv2.circle(mask, center, radius, 255, thickness)
    edge_strength = cv2.mean(edge_image, mask=mask)[0]
    return edge_strength


def compute_angle_between_lines(p1_start, p1_end, p2_start, p2_end):
    """
    Computes angle between two lines: (p1_start -> p1_end) and (p2_start -> p2_end)
    """
    v1 = np.array([p1_end[0] - p1_start[0], p1_end[1] - p1_start[1]])
    v2 = np.array([p2_end[0] - p2_start[0], p2_end[1] - p2_start[1]])

    v1_norm = np.linalg.norm(v1)
    v2_norm = np.linalg.norm(v2)

    if v1_norm == 0 or v2_norm == 0:
        return None

    cos_theta = np.dot(v1, v2) / (v1_norm * v2_norm)
    cos_theta = np.clip(cos_theta, -1.0, 1.0)

    angle = math.degrees(math.acos(cos_theta))

    return angle


def compute_angle(p1, p2, p3):
    """
    Computes the angle at p2 formed by p1 -> p2 -> p3
    Points must be (x, y) tuples.
    """
    v1 = np.array([p1[0] - p2[0], p1[1] - p2[1]])
    v2 = np.array([p3[0] - p2[0], p3[1] - p2[1]])

    # Normalize vectors
    v1_norm = np.linalg.norm(v1)
    v2_norm = np.linalg.norm(v2)

    if v1_norm == 0 or v2_norm == 0:
        return None  # Prevent division by zero

    cos_theta = np.dot(v1, v2) / (v1_norm * v2_norm)

    # Clamp cos_theta to [-1, 1] to avoid math domain error due to float rounding
    cos_theta = np.clip(cos_theta, -1.0, 1.0)

    angle = math.degrees(math.acos(cos_theta))

    return angle


def detect_wheels_and_pose_live():
    cap = cv2.VideoCapture(0)

    if not cap.isOpened():
        print("Error opening webcam.")
        return

    collected_circles = []
    frames_to_collect = 15
    collected_frames = 0
    locked_circles = None

    mp_pose = mp.solutions.pose
    pose = mp_pose.Pose()

    while True:
        ret, frame = cap.read()
        if not ret:
            print("Failed to grab frame.")
            break

        img = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        img_blur = cv2.medianBlur(img, 5)
        output = frame.copy()

        height, width = img.shape

        min_radius = int(min(height, width) / 4.5)
        max_radius = int(min(height, width) / 3.5)
        min_dist = min_radius

        edges = cv2.Canny(img_blur, 100, 200)

        circles = cv2.HoughCircles(
            img_blur,
            cv2.HOUGH_GRADIENT,
            dp=1,
            minDist=min_dist,
            param1=50,
            param2=30,
            minRadius=min_radius,
            maxRadius=max_radius
        )

        # --- Wheel detection ---
        if circles is not None:
            circles = np.uint16(np.around(circles))
            circle_confidences = []
            for i in circles[0, :]:
                center = (i[0], i[1])
                radius = i[2]
                confidence = compute_circle_edge_strength(edges, center, radius)
                circle_confidences.append((center, radius, confidence))

            sorted_circles = sorted(circle_confidences, key=lambda x: x[2], reverse=True)

            for center, radius, confidence in sorted_circles[:2]:
                collected_circles.append((center[0], center[1], radius))

        collected_frames += 1

        if collected_frames >= frames_to_collect:
            if len(collected_circles) >= 2:
                circles_array = np.array(collected_circles)

                criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 100, 0.2)
                _, labels, centers = cv2.kmeans(
                    data=circles_array.astype(np.float32),
                    K=2,
                    bestLabels=None,
                    criteria=criteria,
                    attempts=10,
                    flags=cv2.KMEANS_PP_CENTERS
                )

                locked_circles = centers.astype(int)
                print(f"Refreshed locked circles at frame {collected_frames}: {locked_circles}")

            collected_circles = []
            collected_frames = 0

        # Draw locked wheels
        centers = []
        if locked_circles is not None:
            for circle in locked_circles:
                center = (circle[0], circle[1])
                radius = circle[2]
                centers.append(center)
                cv2.circle(output, center, radius, (0, 255, 0), 2)
                cv2.circle(output, center, 2, (0, 0, 255), 3)

            if len(centers) == 2:
                cv2.line(output, centers[0], centers[1], (255, 0, 0), 2)  # Blue line between wheels

        # --- Pose detection ---
        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        result = pose.process(frame_rgb)

        knee_pos = None
        ankle_pos = None

        if result.pose_landmarks:
            # Landmarks we care about
            body_parts = {
                "left_hip": 23,
                "right_hip": 24,
                "left_knee": 25,
                "right_knee": 26,
                "left_ankle": 27,
                "right_ankle": 28
            }

            landmarks = result.pose_landmarks.landmark
            visibility_threshold = 0.6
            sides = ["left", "right"]
            best_side = None
            best_visibility = -1

            # Pick best visible leg
            for side in sides:
                hip = landmarks[body_parts[f"{side}_hip"]]
                knee = landmarks[body_parts[f"{side}_knee"]]
                ankle = landmarks[body_parts[f"{side}_ankle"]]
                avg_visibility = (hip.visibility + knee.visibility + ankle.visibility) / 3
                if avg_visibility > best_visibility:
                    best_visibility = avg_visibility
                    best_side = side

            if best_side and best_visibility > visibility_threshold:
                hip = landmarks[body_parts[f"{best_side}_hip"]]
                knee = landmarks[body_parts[f"{best_side}_knee"]]
                ankle = landmarks[body_parts[f"{best_side}_ankle"]]

                hip_pos = (int(hip.x * width), int(hip.y * height))
                knee_pos = (int(knee.x * width), int(knee.y * height))
                ankle_pos = (int(ankle.x * width), int(ankle.y * height))

                # Draw points and lines for leg
                cv2.circle(output, hip_pos, 5, (255, 0, 0), -1)
                cv2.circle(output, knee_pos, 5, (255, 0, 0), -1)
                cv2.circle(output, ankle_pos, 5, (255, 0, 0), -1)

                cv2.line(output, hip_pos, knee_pos, (0, 255, 0), 2)
                cv2.line(output, knee_pos, ankle_pos, (0, 255, 0), 2)

                # --- Compute Knee Angle ---
                knee_angle = compute_angle(hip_pos, knee_pos, ankle_pos)
                if knee_angle:
                    cv2.putText(output, f"Knee Angle: {int(knee_angle)} deg", (10, height - 60),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2)

        # --- Compute Perpendicularity (leg vs wheels line) ---
        if len(centers) == 2 and knee_pos is not None and ankle_pos is not None:
            perpendicularity_angle = compute_angle_between_lines(
                centers[0], centers[1],
                knee_pos, ankle_pos
            )
            if perpendicularity_angle is not None:
                # Green if close to 90°, red otherwise
                if abs(perpendicularity_angle - 90) <= 10:
                    color = (0, 255, 0)  # Green
                else:
                    color = (0, 0, 255)  # Red

                cv2.putText(output, f"Perpendicular Angle: {int(perpendicularity_angle)} deg", (10, height - 30),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.8, color, 2)

        # Display progress of frame collection
        cv2.putText(output, f"Collecting: {collected_frames}/{frames_to_collect}", (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 0, 0), 2)

        # Show frame
        cv2.imshow('Detected Wheels + Pose + Angles', output)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    detect_wheels_and_pose_live()
