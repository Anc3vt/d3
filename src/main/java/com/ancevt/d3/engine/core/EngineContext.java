package com.ancevt.d3.engine.core;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EngineContext {

    private final Engine engine;
    private final LaunchConfig launchConfig;
}
