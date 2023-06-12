package swe4.gui.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private final String name;
    private final String password;
    private final String shortName;
    @Serial
    private static final long serialVersionUID = -3813103158851917744L;

    public User(String name, String password, String shortName) {
        this.name = name;
        this.password = password;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User: " +
                "name='" + name + '\'' +
                ", shortName='" + shortName + '\'';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        User otherUser = (User) obj;

        return Objects.equals(name, otherUser.name);
    }
}
