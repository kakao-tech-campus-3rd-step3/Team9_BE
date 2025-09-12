package com.pado.domain.material.event;

import java.util.List;

public record MaterialDeletedEvent(
        List<String> fileKeys
) {}