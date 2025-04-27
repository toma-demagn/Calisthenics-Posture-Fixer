/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package org.tensorflow.lite.examples.poseestimation

import android.graphics.*
import org.tensorflow.lite.examples.poseestimation.data.BodyPart
import org.tensorflow.lite.examples.poseestimation.data.Person
import kotlin.math.max

object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 6f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 4f

    /** The text size of the person id that will be displayed when the tracker is available.  */
    private const val PERSON_ID_TEXT_SIZE = 30f

    /** Distance from person id to the nose keypoint.  */
    private const val PERSON_ID_MARGIN = 6f

    /** Pair of keypoints to draw lines between.  */
    private val bodyJointsRight = listOf(
        //Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        //Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        //Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        //Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        //Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        //Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        //Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        //Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        //Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        //Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        //Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        //Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        //Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    /** Pair of keypoints to draw lines between.  */
    private val bodyJointsLeft = listOf(
        //Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        //Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        //Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        //Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        //Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        //Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        //Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        //Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        //Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        //Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        //Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        //Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        //Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    val leftWrist = 9
    val leftAnkle = 15
    val leftParts = intArrayOf(5,7, 9,11,13,15)
    val leftPartsToCorrect = intArrayOf(5,7,11,13)
    val rightWrist = 10
    val rightAnkle = 16
    val rightParts = intArrayOf(6,8,10,12,14,16)
    val rightPartsToCorrect = intArrayOf(6,8,12,14)
    val bpToText = arrayOf("NOSE", "LEFT_EYE", "RIGHT_EYE","LEFT_EAR", "LEFT_SHOULDER","RIGHT_SHOULDER", "LEFT_ELBOW","RIGHT_ELBOW", "LEFT_WRIST","RIGHT_WRIST", "LEFT_HIP","RIGHT_HIP",
        "LEFT_KNEE","RIGHT_KNEE", "LEFT_ANKLE", "RIGHT_ANKLE")
    var y = 0f
    var hasDrawnAnything = false

    var paint2 = Paint()
    var paintGood = Paint()
    var paintBad = Paint()


    // Draw line and point indicate body pose
    fun drawBodyKeypoints(
        input: Bitmap,
        persons: List<Person>,
        isTrackerEnabled: Boolean = false
    ): Bitmap {
        val paintCircle = Paint().apply {
            strokeWidth = CIRCLE_RADIUS
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
        val paintLine = Paint().apply {
            strokeWidth = LINE_WIDTH
            color = Color.YELLOW
            style = Paint.Style.STROKE
        }

        val paintText = Paint().apply {
            textSize = 20f
            color = Color.RED
            textAlign = Paint.Align.LEFT
        }

        val paintText2 = Paint().apply {
            textSize = 20f
            color = Color.GREEN
            textAlign = Paint.Align.LEFT
        }

        paint2 = Paint().apply {
            strokeWidth = LINE_WIDTH
            color = Color.BLUE
            style = Paint.Style.STROKE
        }

        paintBad = Paint().apply {
            strokeWidth = LINE_WIDTH
            color = Color.RED
            style = Paint.Style.STROKE
        }

        paintGood = Paint().apply {
            strokeWidth = LINE_WIDTH
            color = Color.GREEN
            style = Paint.Style.STROKE
        }

        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val originalSizeCanvas = Canvas(output)
        persons.forEach { person ->
            // draw person id if tracker is enable
            if (isTrackerEnabled) {
                person.boundingBox?.let {
                    val personIdX = max(0f, it.left)
                    val personIdY = max(0f, it.top)

                    originalSizeCanvas.drawText(
                        person.id.toString(),
                        personIdX,
                        personIdY - PERSON_ID_MARGIN,
                        paintText
                    )
                    originalSizeCanvas.drawRect(it, paintLine)
                }
            }

            var leftScore = 1f
            for (i in leftParts)
                leftScore *= person.keyPoints[i].score
            var rightScore = 1f
            for (i in rightParts)
                rightScore *= person.keyPoints[i].score
            //small hack
            leftScore = leftScore + rightScore
            val bodyJoints = if (leftScore > rightScore) bodyJointsLeft else bodyJointsRight
            val wrist = if (leftScore > rightScore) leftWrist else rightWrist
            val ankle = if (leftScore > rightScore) leftAnkle else rightAnkle
            val parts = if (leftScore > rightScore) leftParts else rightParts
            val partsToCorrect = if (leftScore > rightScore) leftPartsToCorrect else rightPartsToCorrect

            bodyJoints.forEach {
                val pointA = person.keyPoints[it.first.position].coordinate
                val pointB = person.keyPoints[it.second.position].coordinate
                originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
            }
            val pointA = person.keyPoints[wrist].coordinate
            val pointB = person.keyPoints[ankle].coordinate
            originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paint2)

            for (i in parts) {
                val point = person.keyPoints[i]
                originalSizeCanvas.drawCircle(
                    point.coordinate.x,
                    point.coordinate.y,
                    CIRCLE_RADIUS,
                    paintCircle
                )
            }
            y = 0f
            hasDrawnAnything = false
            for (i in partsToCorrect){
                originalSizeCanvas.drawText(correctHSForm(person.keyPoints[i].coordinate, person, originalSizeCanvas, wrist, ankle, i), 50f, y, paintText)
            }
            if (!hasDrawnAnything)
                originalSizeCanvas.drawText("Perfect!", 50f, 50f, paintText2)


        }
        return output
    }

    private fun vect(a: FloatArray, b: FloatArray): FloatArray {
        return floatArrayOf(b[0] - a[0], b[1] - a[1])
    }

    private fun dotProd(a: FloatArray, b: FloatArray): Float {
        return b[0] * a[0] + b[1] * a[1]
    }

    private fun scale(a: FloatArray, s: Float): FloatArray {
        return floatArrayOf(a[0]*s, a[1]*s)
    }

    private fun add(a: FloatArray, b: FloatArray): FloatArray {
        return floatArrayOf(a[0]+b[0], a[1]+b[1])
    }

    private fun norm(a: FloatArray): Float {
        return Math.sqrt((a[0] * a[0] + a[1] * a[1]).toDouble()).toFloat()
    }

    private fun correctHSForm(
        point: PointF,
        person: Person,
        canvas: Canvas,
        wrist: Int,
        ankle: Int,
        index: Int
    ): String {
        val adjustedPosition = floatArrayOf(person.keyPoints[wrist].coordinate.x, person.keyPoints[wrist].coordinate.y)
        val adjustedPosition1 = floatArrayOf(person.keyPoints[ankle].coordinate.x, person.keyPoints[ankle].coordinate.y)
        val adjustedPosition2 = floatArrayOf(point.x, point.y)
        val wToA = vect(adjustedPosition, adjustedPosition1)
        val wToAN = scale(wToA, 1/norm(wToA))
        val dp: Float = dotProd(vect(adjustedPosition, adjustedPosition2), wToAN)
        val projected: FloatArray = add(adjustedPosition, scale(wToAN, dp))
        canvas.drawCircle(projected[0], projected[1], CIRCLE_RADIUS, paint2)
        val diff: Float = norm(vect(adjustedPosition2, projected))
        var text = ""
        if (diff > norm(wToA) / 20f){
            hasDrawnAnything = true
            y +=50f
            text = "Move ".plus(bpToText[index])
            val left = (ankle == 15)
            if ((!left and (adjustedPosition2[0] > projected[0])) or (left and (adjustedPosition2[0] < projected[0])))
                text = text.plus(" back ")
            else
                text = text.plus("forward ")
        }
        val paintOfChoice: Paint = if (diff < norm(wToA) / 20f) paintGood else paintBad
        canvas.drawLine(
            adjustedPosition2[0],
            adjustedPosition2[1],
            projected[0],
            projected[1],
            paintOfChoice
        )
        return text
    }
}
