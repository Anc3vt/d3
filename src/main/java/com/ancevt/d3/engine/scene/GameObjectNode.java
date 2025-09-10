package com.ancevt.d3.engine.scene;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class GameObjectNode extends Node {

    @Getter
    private Mesh mesh;

    @Getter
    private int textureId;

    @Getter
    private Vector3f color = new Vector3f(1,1,1);

    public GameObjectNode(Mesh mesh, int textureId) {
        this.mesh = mesh;
        this.textureId = textureId;
    }

    @Override
    public void render(RenderContext ctx) {
        Matrix4f model = getWorldTransform();
        ctx.renderMesh(mesh, textureId, color, model);
        super.render(ctx);
    }

    public void setColor(float r, float g, float b) { color.set(r,g,b); }

    /** 🔹 Управление повторением текстуры */
    public void setTextureRepeat(float u, float v) {
        if(mesh != null) {
            mesh.scaleUV(u, v);
        }
    }
}
