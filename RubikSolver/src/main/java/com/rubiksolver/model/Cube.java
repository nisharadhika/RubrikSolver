//data representation of cube state
package com.rubiksolver.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a 3x3 Rubik's Cube with six faces.
 * Each face is represented as a 3x3 grid of colors.
 */
public class Cube {

    public enum Color {
        WHITE('W'), YELLOW('Y'), RED('R'), ORANGE('O'), BLUE('B'), GREEN('G');

        private final char symbol;

        Color(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }

        public static Color fromSymbol(char symbol) {
            for (Color color : values()) {
                if (color.symbol == symbol) {
                    return color;
                }
            }
            throw new IllegalArgumentException("Unknown color symbol: " + symbol);
        }
    }

    public enum Face {
        FRONT(0), BACK(1), LEFT(2), RIGHT(3), UP(4), DOWN(5);

        private final int index;

        Face(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    // 6 faces, each 3x3 grid
    private Color[][][] faces;

    /**
     * Creates a solved cube with standard color arrangement
     */
    public Cube() {
        faces = new Color[6][3][3];
        initializeSolvedCube();
    }

    /**
     * Creates a cube from a given state
     */
    public Cube(Color[][][] faces) {
        this.faces = new Color[6][3][3];
        for (int f = 0; f < 6; f++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    this.faces[f][i][j] = faces[f][i][j];
                }
            }
        }
    }

    private void initializeSolvedCube() {
        // Standard color arrangement
        fillFace(Face.FRONT, Color.GREEN);
        fillFace(Face.BACK, Color.BLUE);
        fillFace(Face.LEFT, Color.ORANGE);
        fillFace(Face.RIGHT, Color.RED);
        fillFace(Face.UP, Color.WHITE);
        fillFace(Face.DOWN, Color.YELLOW);
    }

    private void fillFace(Face face, Color color) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                faces[face.getIndex()][i][j] = color;
            }
        }
    }

    /**
     * Gets the color at a specific position on a face
     */
    public Color getColor(Face face, int row, int col) {
        return faces[face.getIndex()][row][col];
    }

    /**
     * Sets the color at a specific position on a face
     */
    public void setColor(Face face, int row, int col, Color color) {
        faces[face.getIndex()][row][col] = color;
    }

    /**
     * Gets the entire face as a 2D array
     */
    public Color[][] getFace(Face face) {
        Color[][] faceColors = new Color[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                faceColors[i][j] = faces[face.getIndex()][i][j];
            }
        }
        return faceColors;
    }

    /**
     * Sets an entire face
     */
    public void setFace(Face face, Color[][] faceColors) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                faces[face.getIndex()][i][j] = faceColors[i][j];
            }
        }
    }

    /**
     * Rotates a face clockwise
     */
    public void rotateFaceClockwise(Face face) {
        Color[][] originalFace = getFace(face);
        Color[][] rotatedFace = new Color[3][3];

        // Rotate the face itself
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                rotatedFace[j][2-i] = originalFace[i][j];
            }
        }
        setFace(face, rotatedFace);

        // Rotate adjacent edges
        rotateAdjacentEdges(face, true);
    }

    /**
     * Rotates a face counter-clockwise
     */
    public void rotateFaceCounterClockwise(Face face) {
        // Rotate clockwise 3 times = counter-clockwise once
        rotateFaceClockwise(face);
        rotateFaceClockwise(face);
        rotateFaceClockwise(face);
    }

    private void rotateAdjacentEdges(Face face, boolean clockwise) {
        Color[] temp = new Color[3];

        switch (face) {
            case FRONT:
                if (clockwise) {
                    // Save UP bottom row
                    for (int i = 0; i < 3; i++) temp[i] = faces[Face.UP.getIndex()][2][i];
                    // UP bottom <- LEFT right column
                    for (int i = 0; i < 3; i++) faces[Face.UP.getIndex()][2][i] = faces[Face.LEFT.getIndex()][2-i][2];
                    // LEFT right <- DOWN top row
                    for (int i = 0; i < 3; i++) faces[Face.LEFT.getIndex()][2-i][2] = faces[Face.DOWN.getIndex()][0][i];
                    // DOWN top <- RIGHT left column
                    for (int i = 0; i < 3; i++) faces[Face.DOWN.getIndex()][0][i] = faces[Face.RIGHT.getIndex()][2-i][0];
                    // RIGHT left <- temp
                    for (int i = 0; i < 3; i++) faces[Face.RIGHT.getIndex()][2-i][0] = temp[i];
                }
                break;
            case UP:
                if (clockwise) {
                    // Save FRONT top row
                    for (int i = 0; i < 3; i++) temp[i] = faces[Face.FRONT.getIndex()][0][i];
                    // FRONT top <- RIGHT top
                    for (int i = 0; i < 3; i++) faces[Face.FRONT.getIndex()][0][i] = faces[Face.RIGHT.getIndex()][0][i];
                    // RIGHT top <- BACK top
                    for (int i = 0; i < 3; i++) faces[Face.RIGHT.getIndex()][0][i] = faces[Face.BACK.getIndex()][0][i];
                    // BACK top <- LEFT top
                    for (int i = 0; i < 3; i++) faces[Face.BACK.getIndex()][0][i] = faces[Face.LEFT.getIndex()][0][i];
                    // LEFT top <- temp
                    for (int i = 0; i < 3; i++) faces[Face.LEFT.getIndex()][0][i] = temp[i];
                }
                break;
            case RIGHT:
                if (clockwise) {
                    // Save UP right column
                    for (int i = 0; i < 3; i++) temp[i] = faces[Face.UP.getIndex()][i][2];
                    // UP right <- FRONT right
                    for (int i = 0; i < 3; i++) faces[Face.UP.getIndex()][i][2] = faces[Face.FRONT.getIndex()][i][2];
                    // FRONT right <- DOWN right
                    for (int i = 0; i < 3; i++) faces[Face.FRONT.getIndex()][i][2] = faces[Face.DOWN.getIndex()][i][2];
                    // DOWN right <- BACK left
                    for (int i = 0; i < 3; i++) faces[Face.DOWN.getIndex()][i][2] = faces[Face.BACK.getIndex()][2-i][0];
                    // BACK left <- temp
                    for (int i = 0; i < 3; i++) faces[Face.BACK.getIndex()][2-i][0] = temp[i];
                }
                break;
            // Add cases for other faces as needed
        }
    }

    /**
     * Checks if the cube is solved
     */
    public boolean isSolved() {
        for (Face face : Face.values()) {
            Color centerColor = faces[face.getIndex()][1][1];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (faces[face.getIndex()][i][j] != centerColor) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Scrambles the cube with random moves
     */
    public void scramble(int moves) {
        Face[] allFaces = Face.values();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < moves; i++) {
            Face randomFace = allFaces[random.nextInt(allFaces.length)];
            if (random.nextBoolean()) {
                rotateFaceClockwise(randomFace);
            } else {
                rotateFaceCounterClockwise(randomFace);
            }
        }
    }

    /**
     * Creates a deep copy of the cube
     */
    public Cube copy() {
        return new Cube(this.faces);
    }

    /**
     * Returns string representation of the cube
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Face face : Face.values()) {
            sb.append(face.name()).append(":\n");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    sb.append(faces[face.getIndex()][i][j].getSymbol()).append(" ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Converts cube state to a string format suitable for solving algorithms
     */
    public String toSolverString() {
        StringBuilder sb = new StringBuilder();
        // Standard cube notation: U R F D L B (Up Right Front Down Left Back)
        Face[] solverOrder = {Face.UP, Face.RIGHT, Face.FRONT, Face.DOWN, Face.LEFT, Face.BACK};

        for (Face face : solverOrder) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    sb.append(faces[face.getIndex()][i][j].getSymbol());
                }
            }
        }
        return sb.toString();
    }
}