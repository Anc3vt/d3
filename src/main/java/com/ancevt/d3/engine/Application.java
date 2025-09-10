package com.ancevt.d3.engine;

public interface Application {

    void init(EngineContext ctx);

    void update();

    void shutdown();
}
