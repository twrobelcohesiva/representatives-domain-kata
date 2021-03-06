package rdk.model;

import java.util.HashSet;
import java.util.Set;

import rdk.exception.UnauthorizedAccessException;

public class User {

    private static final User USER_NO_ORGANISATION = UserBuilder.user("without organisation").withRole(UserRole.REGULAR)
            .build();

    private String name;

    private UserRole role;

    private Set<User> promoters;
    
    public User() {
    }

    public static User userWithoutOrganisation() {
        return USER_NO_ORGANISATION;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }
    
    public static class UserBuilder {

        private String name;

        private UserRole role;
        
        public UserBuilder(String name) {
            this.name = name;
        }

        public static UserBuilder user(String name) {
            return new UserBuilder(name);
        }

        public UserBuilder withRole(UserRole role) {
            this.role = role;
            return this;
        }
        
        public User build() {
            User user = new User();

            user.name = this.name;
            user.role = this.role;

            return user;
        }

    }

    public void setOwnerRole() {
        this.role = UserRole.OWNER;
    }

    public void setRepresentativeRole() {
        this.role = UserRole.REPRESENTATIVE;
    }

    public Set<User> getPromoters() {
        if (promoters == null) {
            promoters = new HashSet<User>();
        }
        return promoters;
    }

    public void cancelRepresentativeRole() {
        this.role = UserRole.REGULAR;
    }
    
    public void promoteBy(User promotor) throws UnauthorizedAccessException {
        if (promotor.getRole().equals(UserRole.REPRESENTATIVE)) {
            getPromoters().add(promotor);
        } else {
            throw new UnauthorizedAccessException("User can be promoted only by representative users");
        }
    }
}
