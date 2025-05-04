# Calisthenics Posture Correction Toolkit

This monorepo contains tools to help improve posture and alignment in calisthenics exercises like handstands and squats using pose estimation.

## Projects

### 🧍‍♂️ Android App – Calisthenics Posture Fixer

A mobile application built with Android and Posenet to detect and correct user posture during bodyweight exercises.

**Features:**

* Real-time posture analysis during exercises like handstand and squat.
* Visual feedback and tips based on Posenet landmarks.
* Offline-first design for use in gyms or outdoor environments.

> 📁 Location: `calisthenics-posture-fixer/`

---

### 🚴‍♂️ Python – Bikefitting Pose and Wheel Alignment Tool

A lightweight desktop tool using webcam input to help perform bikefitting. It detects the wheels of the bike and performs posture estimation to generate feedback.

![Demo](bikefitting/asset/bikefitting.gif)

**Features:**

* Detects bike wheels using Hough Circles.
* Uses Mediapipe Pose to extract leg landmarks.
* Calculates knee joint angle.
* Calculates the perpendicularity angle between leg and wheel axis.
* Visual feedback with OpenCV.

> 📁 Location: `bikefitting/`

---

## License

MIT License.
You are free to use, modify, and distribute the tools in this repo.
