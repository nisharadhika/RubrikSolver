package com.rubiksolver.vision;

import com.rubiksolver.model.Cube;
import com.rubiksolver.solver.CubeSolver;

public class App {
    public static void main(String[] args) {
        // Initialize cube object
        Cube cube = new Cube();
        System.out.println("Rubik's Cube Solver Initialized.");

        // Create solver object
        CubeSolver solver = new CubeSolver();

        // Call solver and get solution string
        String solution = solver.solve(cube);

        // Output the solution
        System.out.println("Solution: " + solution);
    }
}