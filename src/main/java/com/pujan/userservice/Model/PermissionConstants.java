package com.pujan.userservice.Model;

public class PermissionConstants {

    // Academic permissions
    public static final String ACADEMIC_READ = "PROGRAM:ACADEMIC:READ";
    public static final String ACADEMIC_UPDATE = "PROGRAM:ACADEMIC:UPDATE";

    // Programme permissions
    public static final String PROGRAMME_READ = "PROGRAM:PROGRAMME:READ";
    public static final String PROGRAMME_UPDATE = "PROGRAM:PROGRAMME:UPDATE";

    // IAM permissions
    public static final String MANAGE_USER_PERMISSIONS = "IAM:MANAGE_USER_PERMISSIONS";

    // Utility methods
    public static String[] getAllPermissions() {
        return new String[] {
            ACADEMIC_READ, ACADEMIC_UPDATE,
            PROGRAMME_READ, PROGRAMME_UPDATE,
            MANAGE_USER_PERMISSIONS
        };
    }

    public static String[] getPermissionsForRole(String roleName) {
        switch (roleName) {
            case "ACADEMIC_VIEWER":
                return new String[] {ACADEMIC_READ};
            case "ACADEMIC_EDITOR":
                return new String[] {ACADEMIC_READ, ACADEMIC_UPDATE};
            case "PROGRAMME_VIEWER":
                return new String[] {PROGRAMME_READ};
            case "PROGRAMME_EDITOR":
                return new String[] {PROGRAMME_READ, PROGRAMME_UPDATE};
            case "ROLE_SUPER_ADMIN":
                return getAllPermissions();
            case "ROLE_ADMIN":
                return new String[] {MANAGE_USER_PERMISSIONS};
            default:
                return new String[0];
        }
    }
}

