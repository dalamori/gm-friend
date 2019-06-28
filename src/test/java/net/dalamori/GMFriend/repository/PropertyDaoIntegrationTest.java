package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.testing.IntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class PropertyDaoIntegrationTest {

    @Autowired
    public PropertyDao propertyDao;

    private Property property;

    public static final String PROPERTY_NAME = "propName";
    public static final String PROPERTY_VALUE = "TestValue - Foo";
    public static final String PROPERTY_OWNER = "SomeTestGuy";

    @Before
    public void setUp() {
        propertyDao.deleteAll();

        property = new Property();
        property.setName(PROPERTY_NAME);
        property.setValue(PROPERTY_VALUE);
        property.setOwner(PROPERTY_OWNER);
        property.setPrivacy(PrivacyType.NORMAL);
        property.setType(PropertyType.STRING);
    }

    @Test
    public void propertyDao_save_shouldHappyPath() {
        // when: I save a property
        Property result = propertyDao.save(property);

        // then: I expect to get a result with an ID
        Assert.assertTrue("got result", result instanceof Property);
        Assert.assertTrue("got an id", result.getId() instanceof Long);

        // and: I should be able to read that property back with the correct data
        Property findResult = propertyDao.findById(result.getId()).get();

        Assert.assertEquals("lookup returns a copy with the correct name", PROPERTY_NAME, findResult.getName());
        Assert.assertEquals("lookup returns a copy with the correct value", PROPERTY_VALUE, findResult.getValue());
        Assert.assertEquals("lookup returns a copy with the correct owner", PROPERTY_OWNER, findResult.getOwner());
        Assert.assertEquals("lookup returns a copy with the correct privacy", PrivacyType.NORMAL, findResult.getPrivacy());
        Assert.assertEquals("lookup returns a copy with the correct property type", PropertyType.STRING, findResult.getType());
    }
}
