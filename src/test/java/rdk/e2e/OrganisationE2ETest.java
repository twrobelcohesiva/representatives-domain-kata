package rdk.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static rdk.assertions.UserAssert.assertThat;
import static rdk.model.User.UserBuilder.user;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import rdk.exception.UnauthorizedAccessException;
import rdk.init.ApplicationConfig;
import rdk.model.Organisation;
import rdk.model.User;
import rdk.model.UserRole;
import rdk.service.OrganisationService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public class OrganisationE2ETest {

    private static final String REGULAR_TEST_USER = "regular user";
    private static final int DEFAULT_NUM_OF_ACKNOWLEDGMENTS = 2;
    
    @Autowired
    OrganisationService organisationService;

    User owner;
    Organisation testOrganisation;

    @Before
    public void init() {
        owner = user(REGULAR_TEST_USER).withRole(UserRole.REGULAR).build();
        
        testOrganisation = organisationService.createNewOrganisation("nazwa", owner);
    }
    
    @Test
    public void newOrganisationIsInActive() {
        assertThat(testOrganisation.isActive()).isFalse();
    }
    
    @Test
    public void userBecomeOwnerOfNewCreatedOrganisation() {
        assertThat(owner).hasRole(UserRole.OWNER);
        assertThat(owner).isOwnerOfOrganisation(testOrganisation);
    }

    @Test
    public void ownerRequestsForActivation() throws UnauthorizedAccessException {

        organisationService.requestForActivation(testOrganisation, owner);

        assertThat(testOrganisation.isActivationAwaiting()).isTrue();
    }
    
    @Test
    public void addsMemberToNewInActiveOrganisation() throws UnauthorizedAccessException {
        User newMember = user("newMember").withRole(UserRole.REGULAR).build();
        
        assertThat(testOrganisation.isActive()).isFalse();
        assertThat(newMember).hasRole(UserRole.REGULAR);
        
        organisationService.addMember(testOrganisation, owner, newMember);
        
        assertThat(newMember).hasRole(UserRole.REPRESENTATIVE);
        assertThat(newMember).isInOrganisationMembers(testOrganisation);
    }
    
    @Test
    public void ownerSetsRequiredNumberOfAcknowledgmentsForRepresentativeUser() throws UnauthorizedAccessException {
        
        organisationService.setNumOfRequiredAcknowledgments(testOrganisation, DEFAULT_NUM_OF_ACKNOWLEDGMENTS, owner);
        
        assertThat(testOrganisation.getNumOfAcknowledgments()).isEqualTo(DEFAULT_NUM_OF_ACKNOWLEDGMENTS);
    }
    
    @Test
    public void organisationIsActivatedByAdmin() throws UnauthorizedAccessException {
        User admin = user("Admin user").withRole(UserRole.ADMIN).build();
        
        organisationService.activateOrganisation(testOrganisation, admin);
        
        assertThat(testOrganisation.isActive()).isTrue();
    }
    
    @Test
    public void userGetsProperNumOfAcknoledgmentsAndBecomeRepresentative() throws UnauthorizedAccessException {
        List<User> someRepresentativeMembers = prepareMembers(3);
        
        organisationService.addMember(testOrganisation, owner, someRepresentativeMembers.get(0));
        organisationService.addMember(testOrganisation, owner, someRepresentativeMembers.get(1));
        organisationService.addMember(testOrganisation, owner, someRepresentativeMembers.get(2));
        
        
    }
    
    private List<User> prepareMembers(int num) {
        
        List<User> users = new ArrayList<User>();
        
        for (int i = 0; i < num; i++) {
            users.add(user("user " + (i + 1)).withRole(UserRole.REPRESENTATIVE).build());
        }
        
        return users;
        
    }
}
