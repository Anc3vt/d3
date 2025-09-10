package com.ancevt.d3.engine.core;

public interface Application {

    void init(EngineContext ctx);

    void update();

    void shutdown();
}
