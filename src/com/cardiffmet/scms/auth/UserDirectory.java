package com.cardiffmet.scms.auth;

import com.cardiffmet.scms.model.UserAccount;
import com.cardiffmet.scms.model.UserRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory user accounts for the prototype (plain passwords — not for production).
 */
public final class UserDirectory {
    private final Map<String, UserAccount> byUsername = new LinkedHashMap<>();

    public UserDirectory() {
    }

    public static UserDirectory withDemoAccounts() {
        UserDirectory d = new UserDirectory();
        d.putAccount(new UserAccount("admin", "admin123", "Alex Administrator", UserRole.ADMINISTRATOR));
        d.putAccount(new UserAccount("staff", "staff123", "Jordan Staff", UserRole.STAFF_MEMBER));
        d.putAccount(new UserAccount("student", "student123", "Taylor Student", UserRole.STUDENT));
        return d;
    }

    private void putAccount(UserAccount account) {
        byUsername.put(account.getUsername().toLowerCase(Locale.ROOT), account);
    }

    public Optional<UserAccount> find(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byUsername.get(username.trim().toLowerCase(Locale.ROOT)));
    }

    /**
     * Validates credentials and that the claimed role matches the account role.
     */
    public boolean authenticate(String username, char[] password, UserRole claimedRole) {
        Objects.requireNonNull(claimedRole);
        Optional<UserAccount> opt = find(username);
        if (opt.isEmpty()) {
            clear(password);
            return false;
        }
        UserAccount ua = opt.get();
        if (ua.getRole() != claimedRole) {
            clear(password);
            return false;
        }
        String passStr = password == null ? "" : new String(password);
        clear(password);
        return ua.getPassword().equals(passStr);
    }

    private static void clear(char[] password) {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
        }
    }

    public List<UserAccount> listAccounts() {
        return Collections.unmodifiableList(new ArrayList<>(byUsername.values()));
    }

    /** Usernames with the given role (lower-case login names). */
    public List<String> listUsernamesWithRole(UserRole role) {
        Objects.requireNonNull(role);
        return byUsername.values().stream()
                .filter(a -> a.getRole() == role)
                .map(UserAccount::getUsername)
                .collect(Collectors.toList());
    }

    /**
     * @return false if username already exists
     */
    public boolean addAccount(String username, String password, String displayName, UserRole role) {
        String key = username.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty() || byUsername.containsKey(key)) {
            return false;
        }
        putAccount(new UserAccount(key, password, displayName, role));
        return true;
    }

    public boolean removeAccount(String username) {
        String key = username.trim().toLowerCase(Locale.ROOT);
        return byUsername.remove(key) != null;
    }

    public Optional<SessionInfo> resolveSession(String username) {
        return find(username).map(ua -> new SessionInfo(ua.getUsername(), ua.getDisplayName(), ua.getRole()));
    }

    public record SessionInfo(String username, String displayName, UserRole role) {
    }

    public static String loginHint(UserRole role) {
        return switch (role) {
            case ADMINISTRATOR -> "Demo: admin / admin123";
            case STAFF_MEMBER -> "Demo: staff / staff123";
            case STUDENT -> "Demo: student / student123";
        };
    }
}
