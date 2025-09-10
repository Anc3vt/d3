package com.ancevt.d3.engine.scene;

public class MeshFactory {

    public static Mesh createTexturedCubeMesh(float size) {
        float hs = size / 2.0f; // half size

        float[] vertices = {
                // позиции          // UV      // нормали
                // Front
                -hs, -hs,  hs, 0, 0, 0, 0, 1,
                hs, -hs,  hs, 1, 0, 0, 0, 1,
                hs,  hs,  hs, 1, 1, 0, 0, 1,
                hs,  hs,  hs, 1, 1, 0, 0, 1,
                -hs,  hs,  hs, 0, 1, 0, 0, 1,
                -hs, -hs,  hs, 0, 0, 0, 0, 1,

                // Back
                -hs, -hs, -hs, 0, 0, 0, 0, -1,
                -hs,  hs, -hs, 0, 1, 0, 0, -1,
                hs,  hs, -hs, 1, 1, 0, 0, -1,
                hs,  hs, -hs, 1, 1, 0, 0, -1,
                hs, -hs, -hs, 1, 0, 0, 0, -1,
                -hs, -hs, -hs, 0, 0, 0, 0, -1,

                // Left
                -hs,  hs,  hs, 1, 0, -1, 0, 0,
                -hs,  hs, -hs, 1, 1, -1, 0, 0,
                -hs, -hs, -hs, 0, 1, -1, 0, 0,
                -hs, -hs, -hs, 0, 1, -1, 0, 0,
                -hs, -hs,  hs, 0, 0, -1, 0, 0,
                -hs,  hs,  hs, 1, 0, -1, 0, 0,

                // Right
                hs,  hs,  hs, 1, 0, 1, 0, 0,
                hs, -hs, -hs, 0, 1, 1, 0, 0,
                hs,  hs, -hs, 1, 1, 1, 0, 0,
                hs, -hs, -hs, 0, 1, 1, 0, 0,
                hs,  hs,  hs, 1, 0, 1, 0, 0,
                hs, -hs,  hs, 0, 0, 1, 0, 0,

                // Top
                -hs,  hs, -hs, 0, 1, 0, 1, 0,
                -hs,  hs,  hs, 0, 0, 0, 1, 0,
                hs,  hs,  hs, 1, 0, 0, 1, 0,
                hs,  hs,  hs, 1, 0, 0, 1, 0,
                hs,  hs, -hs, 1, 1, 0, 1, 0,
                -hs,  hs, -hs, 0, 1, 0, 1, 0,

                // Bottom
                -hs, -hs, -hs, 0, 1, 0, -1, 0,
                hs, -hs,  hs, 1, 0, 0, -1, 0,
                -hs, -hs,  hs, 0, 0, 0, -1, 0,
                hs, -hs,  hs, 1, 0, 0, -1, 0,
                -hs, -hs, -hs, 0, 1, 0, -1, 0,
                hs, -hs, -hs, 1, 1, 0, -1, 0
        };

        return new Mesh(vertices, 8);
    }
}
