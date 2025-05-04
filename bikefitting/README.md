# Bikefitting Pose and Wheel Alignment Tool

This project is a simple real-time bike fitting assistant using a webcam. It detects the user's leg pose and the positions of bike wheels, then analyzes the knee angle and the angle between shin and wheel line.

![Demo](asset/bikefitting.gif)

## Features

* Detects bike wheels using Hough Circles.
* Uses Mediapipe Pose to extract leg landmarks.
* Calculates knee joint angle.
* Calculates the perpendicularity angle between leg and wheel axis.
* Visual feedback with OpenCV.

## Usage

### 1. Install dependencies

```bash
pip install -r requirements.txt
```

### 2. Run the live detection

```bash
python main.py
```

Press `q` to quit the live detection window.

## Requirements

See `requirements.txt` for all dependencies.

## Notes

* Ensure you have a webcam connected.
* Good lighting improves pose and edge detection.
* Works best when the entire leg and bike wheels are visible in the frame.

## License

MIT License
