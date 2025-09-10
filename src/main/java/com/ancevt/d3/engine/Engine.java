package com.ancevt.d3.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Engine {
    private final Config config;
    private Window window;
    private ShaderProgram shader;
    private Camera camera;

    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;

    private final float cameraSpeed = 0.03f;
    private final float mouseSensitivity = 0.1f;

    public List<GameObject> objects;

    // Engine.java
    private float velocityY = 0.0f;
    private final float gravity = -0.0005f;
    private final float jumpStrength = 0.055f;
    private boolean isGrounded = false;

    private GameObject groundObj = null; // объект, на котором стоит игрок

    private Vector3f playerSize = new Vector3f(0.1f, 0.2f, 0.1f); // ширина, высота, глубина


    public Engine(Config config) {
        this.config = config;
    }


    public void start(Application application) {

        window = new Window(1280, 720, "LWJGL Multi OBJ Loader");
        window.init();

        prepareEngine();

        application.init(createContext());

        loop();

        application.shutdown();
    }

    private void prepareEngine() {
        camera = new Camera();
        objects = new ArrayList<>();

        // === Шейдеры ===
        shader = new ShaderProgram();
        shader.attachShader(DefaultShaders.VERTEX, GL_VERTEX_SHADER);
        shader.attachShader(DefaultShaders.FRAGMENT, GL_FRAGMENT_SHADER);
        shader.link();

        glUseProgram(shader.getId());
        glUniform1i(glGetUniformLocation(shader.getId(), "texture1"), 0);

        glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(window.getWindowHandle(), (handle, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }

            float dx = (float) (xpos - lastMouseX) * mouseSensitivity;
            float dy = (float) (lastMouseY - ypos) * mouseSensitivity;

            lastMouseX = xpos;
            lastMouseY = ypos;

            camera.addRotation(dx, dy);
        });
    }

    private EngineContext createContext() {
        EngineContext engineContext = new EngineContext(this, config);

        return engineContext;
    }

    private void loop() {
        float fov = (float) Math.toRadians(100.0);
        float aspect = 1280f / 720f;
        float zNear = 0.01f, zFar = 1000f;

        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);

            processInput();
            shader.use();

            float time = (System.currentTimeMillis() % 100000) / 1000.0f; // <<< считаем один раз

            Matrix4f projection = new Matrix4f().perspective(fov, aspect, zNear, zFar);
            Matrix4f view = camera.getViewMatrix();

            int projLoc = glGetUniformLocation(shader.getId(), "projection");
            int viewLoc = glGetUniformLocation(shader.getId(), "view");
            int modelLoc = glGetUniformLocation(shader.getId(), "model");

            int lightPosLoc = glGetUniformLocation(shader.getId(), "lightPos");
            int viewPosLoc = glGetUniformLocation(shader.getId(), "viewPos");
            int lightColorLoc = glGetUniformLocation(shader.getId(), "lightColor");

            try (var stack = MemoryStack.stackPush()) {
                glUniformMatrix4fv(projLoc, false, projection.get(stack.mallocFloat(16)));
                glUniformMatrix4fv(viewLoc, false, view.get(stack.mallocFloat(16)));

                glUniform3fv(lightPosLoc, new float[]{1.2f, 1.0f, 2.0f});
                glUniform3fv(viewPosLoc, camera.getPosition().get(stack.mallocFloat(3)));
                glUniform3f(lightColorLoc, 1.0f, 1.0f, 1.0f);

                for (GameObject obj : objects) {
                    Matrix4f model = obj.getModelMatrix();
                    glUniformMatrix4fv(modelLoc, false, model.get(stack.mallocFloat(16)));

                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, obj.getTextureId());

                    int objectColorLoc = glGetUniformLocation(shader.getId(), "objectColor");
                    glUniform3f(objectColorLoc, obj.getColor().x, obj.getColor().y, obj.getColor().z);

                    // ⬇ передаём время в update
                    obj.update(time);
                    obj.getMesh().render();
                }
            }

            window.update();
        }
    }

    private void processInput() {
        long win = window.getWindowHandle();

        Vector3f front = camera.getFront();
        Vector3f right = front.cross(new Vector3f(0, 1, 0), new Vector3f()).normalize();

        Vector3f moveDir = new Vector3f();

        // Горизонтальное управление
        if (glfwGetKey(win, GLFW_KEY_W) == GLFW_PRESS) moveDir.add(new Vector3f(front).mul(cameraSpeed));
        if (glfwGetKey(win, GLFW_KEY_S) == GLFW_PRESS) moveDir.add(new Vector3f(front).mul(-cameraSpeed));
        if (glfwGetKey(win, GLFW_KEY_A) == GLFW_PRESS) moveDir.add(new Vector3f(right).mul(-cameraSpeed));
        if (glfwGetKey(win, GLFW_KEY_D) == GLFW_PRESS) moveDir.add(new Vector3f(right).mul(cameraSpeed));

        Vector3f pos = new Vector3f(camera.getPosition());

        // Прыжок
        if (glfwGetKey(win, GLFW_KEY_SPACE) == GLFW_PRESS && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        // Применяем гравитацию
        velocityY += gravity;
        moveDir.y += velocityY;

        // --- ПОШАГОВОЕ ДВИЖЕНИЕ ---
        Vector3f newPos = new Vector3f(pos);

        // X
        newPos.x += moveDir.x;
        if (checkCollision(newPos)) {
            newPos.x = pos.x; // отменяем X, но оставляем Y/Z
        }

        // Z
        newPos.z += moveDir.z;
        if (checkCollision(newPos)) {
            newPos.z = pos.z; // отменяем Z
        }

        // Y
        newPos.y += moveDir.y;
        if (checkCollision(newPos)) {
            if (moveDir.y < 0) { // ударились о пол
                isGrounded = true;
            }
            velocityY = 0; // сброс вертикальной скорости
            newPos.y = pos.y; // не двигаем по Y
        } else {
            isGrounded = false;
        }

        camera.getPosition().set(newPos);

        // Выход
        if (glfwGetKey(win, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(win, true);
        }
    }

    private boolean checkCollision(Vector3f newPos) {
        Vector3f half = new Vector3f(playerSize).mul(0.5f);
        AABB playerAABB = new AABB(
                new Vector3f(newPos).sub(half),
                new Vector3f(newPos).add(half)
        );

        for (GameObject obj : objects) {
            AABB box = obj.getBoundingBox();
            if (playerAABB.min.x <= box.max.x && playerAABB.max.x >= box.min.x &&
                    playerAABB.min.y <= box.max.y && playerAABB.max.y >= box.min.y &&
                    playerAABB.min.z <= box.max.z && playerAABB.max.z >= box.min.z) {
                return true;
            }
        }
        return false;
    }



    private Float raycastDown(Vector3f pos, GameObject obj) {
        float groundY = Float.NEGATIVE_INFINITY;

        for (Vector3f[] tri : obj.getMesh().getTriangles()) {
            Vector3f v0 = new Vector3f(tri[0]).add(obj.getPosition());
            Vector3f v1 = new Vector3f(tri[1]).add(obj.getPosition());
            Vector3f v2 = new Vector3f(tri[2]).add(obj.getPosition());

            // Нормаль треугольника
            Vector3f edge1 = new Vector3f(v1).sub(v0);
            Vector3f edge2 = new Vector3f(v2).sub(v0);
            Vector3f normal = edge1.cross(edge2, new Vector3f()).normalize();

            // Если треугольник почти вертикальный — пропускаем (это не пол)
            if (Math.abs(normal.y) < 0.5f) continue;

            // Находим уравнение плоскости
            float d = -normal.dot(v0);

            // Луч вниз: (x, y, z) = pos + t*(0,-1,0)
            float denom = normal.y * -1.0f;
            if (Math.abs(denom) < 1e-6) continue;

            float t = -(normal.dot(pos) + d) / denom;
            if (t < 0) continue;

            float y = pos.y - t;

            // Проверяем, лежит ли (pos.x, y, pos.z) внутри треугольника
            Vector3f p = new Vector3f(pos.x, y, pos.z);
            if (pointInTriangle(p, v0, v1, v2)) {
                if (y > groundY && y <= pos.y) {
                    groundY = y;
                }
            }
        }
        return groundY == Float.NEGATIVE_INFINITY ? null : groundY;
    }

    private boolean pointInTriangle(Vector3f p, Vector3f a, Vector3f b, Vector3f c) {
        Vector3f v0 = new Vector3f(c).sub(a);
        Vector3f v1 = new Vector3f(b).sub(a);
        Vector3f v2 = new Vector3f(p).sub(a);

        float dot00 = v0.dot(v0);
        float dot01 = v0.dot(v1);
        float dot02 = v0.dot(v2);
        float dot11 = v1.dot(v1);
        float dot12 = v1.dot(v2);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v <= 1);
    }


}
