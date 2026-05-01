package com.commonlibrary.dto.schema;

public class SpiceSchema {
    // Objects
    public static final String USER = "user";
    public static final String POST = "post";
    public static final String REEL = "reel";
    public static final String STORY = "story";
    public static final String GROUP = "group";
    public static final String COMMENT = "comment";
    public static final String PLATFORM = "platform";
    public static final String FRIEND = "friend";

    public static final String SYSTEM_PLATFORM_ID = "clyvasync_system";

    // Relations
    public static final String REL_AUTHOR = "author";
    public static final String REL_VIEWER = "viewer";
    public static final String REL_PARENT_GROUP = "parent_group";
    public static final String REL_PARENT_POST = "parent_post";
    public static final String REL_PARENT_REEL = "parent_reel";
    public static final String REL_PARENT_PLATFORM = "parent_platform";
    public static final String REL_MEMBER = "member";
    public static final String REL_INVITEE = "invitee";
    public static final String REL_CREATOR = "creator";
    public static final String REL_ADMIN = "admin";
    public static final String REL_MODERATOR = "moderator";


    // Permissions
    public static final String PERM_VIEW = "view";
    public static final String PERM_EDIT = "edit";
    public static final String PERM_DELETE = "delete";
    public static final String PERM_CREATE = "creator";

    public static final String PERM_ADMIN = "admin";
    public static final String PERM_READ = "read";
    public static final String PERM_WRITE = "write";
    public static final String PERM_MEMBER = "member";
    public static final String PERM_INVITE = "invite";



    // Wildcards
    public static final String WILDCARD_USER = "*";
}