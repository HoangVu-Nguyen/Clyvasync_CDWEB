package com.commonlibrary.constant;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class SpiceDBConstants {

    @Getter
    @RequiredArgsConstructor
    public enum TargetType {
        USER("user"),
        RESOURCE("resource"); // Đại diện cho Photo, Profile, Post... theo Schema của bạn

        private final String value;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Relation {
        FRIEND("friend"),
        OWNER("owner"),
        BLOCKED("blocked");

        private final String value;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Permission {
        VIEW("view"),
        EDIT("edit"),
        DELETE("delete");

        private final String value;
    }
}