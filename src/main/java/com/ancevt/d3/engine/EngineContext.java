package com.ancevt.d3.engine;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EngineContext {

    private final Engine engine;
    private final Config config;
}
