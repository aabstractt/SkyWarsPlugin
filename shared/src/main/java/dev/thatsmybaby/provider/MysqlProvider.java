package dev.thatsmybaby.provider;

import lombok.Getter;

public class MysqlProvider {

    @Getter private final static MysqlProvider instance = new MysqlProvider();

    public void init() {

    }
}