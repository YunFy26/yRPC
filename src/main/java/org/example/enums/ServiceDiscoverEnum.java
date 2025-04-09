package org.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceDiscoverEnum {

    ZK("zk");

    private final String name;
}
