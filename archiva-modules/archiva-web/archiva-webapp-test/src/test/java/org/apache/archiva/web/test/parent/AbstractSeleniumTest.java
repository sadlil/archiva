package org.apache.archiva.web.test.parent;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.web.test.tools.ArchivaSeleniumExecutionRule;
import org.apache.archiva.web.test.tools.WebdriverUtility;
import org.junit.Assert;
import org.junit.Rule;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */

public abstract class AbstractSeleniumTest
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Rule
    public ArchivaSeleniumExecutionRule archivaSeleniumExecutionRule = new ArchivaSeleniumExecutionRule();

    public String browser = System.getProperty( "browser" );

    public String baseUrl =
        "http://localhost:" + System.getProperty( "container.http.port" ) + "/archiva/index.html?request_lang=en";

    public int maxWaitTimeInMs = Integer.getInteger( "maxWaitTimeInMs" );

    public String seleniumHost = System.getProperty( "seleniumHost", "localhost" );

    public int seleniumPort = Integer.getInteger( "seleniumPort", 4444 );

    public boolean remoteSelenium = Boolean.parseBoolean( System.getProperty( "seleniumRemote", "false" ) );

    WebDriver webDriver = null;

    public Properties p;

    /**
     * this method is called by the Rule before executing a test
     *
     * @throws Exception
     */
    public void open()
        throws Exception
    {
        p = new Properties();
        p.load( this.getClass().getClassLoader().getResourceAsStream( "test.properties" ) );

        baseUrl = WebdriverUtility.getBaseUrl()+"/index.html?request_lang=en";

        open( baseUrl, browser, seleniumHost, seleniumPort, maxWaitTimeInMs, remoteSelenium );
        assertAdminCreated();
    }

    /**
     * this method is called by the Rule after executing a tests
     */
    public void close()
    {
        getWebDriver().close();
    }

    /**
     * Initialize selenium
     */
    public void open( String baseUrl, String browser, String seleniumHost, int seleniumPort, int maxWaitTimeInMs, boolean remoteSelenium)
        throws Exception
    {
        try
        {
            if ( getWebDriver() == null )
            {
                WebDriver driver = WebdriverUtility.newWebDriver(browser, seleniumHost, seleniumPort, remoteSelenium);
                // selenium.start();
                // selenium.setTimeout( Integer.toString( maxWaitTimeInMs ) );
                this.webDriver = driver;
            }
        }
        catch ( Exception e )
        {
            // yes
            System.out.print( e.getMessage() );
            e.printStackTrace();
        }
    }

    public void assertAdminCreated()
        throws Exception
    {
        initializeArchiva( baseUrl, browser, maxWaitTimeInMs, seleniumHost, seleniumPort, remoteSelenium );
    }

    public void initializeArchiva( String baseUrl, String browser, int maxWaitTimeInMs, String seleniumHost,
                                   int seleniumPort, boolean remoteSelenium)
        throws Exception
    {

        open( baseUrl, browser, seleniumHost, seleniumPort, maxWaitTimeInMs, remoteSelenium);

        getWebDriver().get(baseUrl);
        WebDriverWait wait = new WebDriverWait(getWebDriver(),30);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("topbar-menu")));

        FluentWait fluentWait = new FluentWait(getWebDriver()).withTimeout(10, TimeUnit.SECONDS);
        fluentWait.until( ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.id("create-admin-link")),
                            ExpectedConditions.visibilityOfElementLocated(By.id("login-link-a"))));


        // if not admin user created create one
        if ( isElementVisible( "create-admin-link" ) )
        {
            Assert.assertFalse( isElementVisible( "login-link-a" ) );
            Assert.assertFalse( isElementVisible( "register-link-a" ) );
            // skygo need to set to true for passing is that work as expected ?
            clickLinkWithLocator( "create-admin-link-a");
            wait = new WebDriverWait(getWebDriver(), 5);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-create")));
            assertCreateAdmin();
            String fullname = getProperty( "ADMIN_FULLNAME" );
            String username = getAdminUsername();
            String mail = getProperty( "ADMIN_EMAIL" );
            String password = getProperty( "ADMIN_PASSWORD" );
            submitAdminData( fullname, mail, password );
            assertUserLoggedIn( username );
            clickLinkWithLocator( "logout-link-a" , false);
        }
        else
        {
            Assert.assertTrue( isElementVisible( "login-link-a" ) );
            Assert.assertTrue( isElementVisible( "register-link-a" ) );
            login( getAdminUsername(), getAdminPassword() );
        }

    }

    public WebDriver getWebDriver() {
        return this.webDriver;
    }

    protected String getProperty( String key )
    {
        return p.getProperty( key );
    }

    public String getAdminUsername()
    {
        String adminUsername = getProperty( "ADMIN_USERNAME" );
        return adminUsername;
    }

    public String getAdminPassword()
    {
        String adminPassword = getProperty( "ADMIN_PASSWORD" );
        return adminPassword;
    }

    public void submitAdminData( String fullname, String email, String password )
    {
        setFieldValue( "fullname", fullname );
        setFieldValue( "email", email );
        setFieldValue( "password", password );
        setFieldValue( "confirmPassword", password );
        clickButtonWithLocator( "user-create-form-register-button" , false);
    }

    public void login( String username, String password )
    {
        login( username, password, true, "Login Page" );
    }

    public void login( String username, String password, boolean valid, String assertReturnPage )
    {
        if ( isElementVisible( "login-link-a" ) )//isElementPresent( "loginLink" ) )
        {
            goToLoginPage();

            submitLoginPage( username, password, false, valid, assertReturnPage );
        }
        if ( valid )
        {
            assertUserLoggedIn( username );
        }
    }

    // Go to Login Page
    public void goToLoginPage()
    {
        logger.info("Goto login page");
        getWebDriver().get( baseUrl );
        WebDriverWait wait = new WebDriverWait(getWebDriver(),30);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("topbar-menu")));
        wait.until(ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(By.id("logout-link")),
                ExpectedConditions.visibilityOfElementLocated(By.id("login-link-a"))));

        // are we already logged in ?
        if ( isElementVisible( "logout-link" ) ) //isElementPresent( "logoutLink" ) )
        {
            logger.info("Logging out ");
            // so logout
            clickLinkWithLocator( "logout-link-a", false );
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-link-a")));
        clickLinkWithLocator( "login-link-a", false );
        // This is a workaround for bug with HTMLUnit. The display attribute of the
        // login dialog is not changed via the click.
        // TODO: Check after changing jquery, bootstrap or htmlunit version
        if (getWebDriver() instanceof HtmlUnitDriver)
        {
            ( (JavascriptExecutor) getWebDriver() ).executeScript( "$('#modal-login').show();" );
        }
        // END OF WORKAROUND
        wait = new WebDriverWait(getWebDriver(),20);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modal-login")));
        assertLoginModal();
    }


    public void assertLoginModal()
    {
        assertElementPresent( "user-login-form" );
        Assert.assertTrue( isElementVisible( "register-link" ) );
        Assert.assertTrue( isElementVisible("user-login-form-username" ));
        Assert.assertTrue( isElementVisible("user-login-form-password" ));
        assertButtonWithIdPresent( "modal-login-ok" );
        Assert.assertTrue( isElementVisible( "modal-login-ok" ));
    }


    public void submitLoginPage( String username, String password )
    {
        submitLoginPage( username, password, false, true, "Login Page" );
    }

    public void submitLoginPage( String username, String password, boolean validUsernamePassword )
    {
        submitLoginPage( username, password, false, validUsernamePassword, "Login Page" );
    }

    public void submitLoginPage( String username, String password, boolean rememberMe, boolean validUsernamePassword,
                                 String assertReturnPage )
    {
        logger.info("Activating login form");
        // clickLinkWithLocator( "login-link-a", false);
        WebDriverWait wait = new WebDriverWait(getWebDriver(),5);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOf(getWebDriver().findElement(By.id("user-login-form-username"))));
        wait = new WebDriverWait(getWebDriver(),5);
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOf(getWebDriver().findElement(By.id("user-login-form-password"))));
        wait = new WebDriverWait(getWebDriver(),5);
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.id("modal-login-ok")));
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        /*
        if ( rememberMe )
        {
            checkField( "rememberMe" );
        }*/

        button.click();
        if ( validUsernamePassword )
        {
            assertUserLoggedIn( username );
        }
        /*
        else
        {
            if ( "Login Page".equals( assertReturnPage ) )
            {
                assertLoginPage();
            }
            else
            {
                assertPage( assertReturnPage );
            }
        }*/
    }

    // *******************************************************
    // Auxiliar methods. This method help us and simplify test.
    // *******************************************************

    protected void assertUserLoggedIn( String username )
    {
        WebDriverWait wait = new WebDriverWait(getWebDriver(), 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout-link")));
        Assert.assertFalse( isElementVisible( "login-link" ) );
        Assert.assertFalse( isElementVisible( "register-link" ) );
        Assert.assertFalse( isElementVisible( "create-admin-link" ) );
    }

    public void assertCreateAdmin()
    {
        assertElementPresent( "user-create" );
        assertFieldValue( "admin", "username" );
        assertElementPresent( "fullname" );
        assertElementPresent( "password" );
        assertElementPresent( "confirmPassword" );
        assertElementPresent( "email" );
    }

    public void assertFieldValue( String fieldValue, String fieldName )
    {
        assertElementPresent( fieldName );
        Assert.assertEquals( fieldValue, findElement(fieldName ).getAttribute( "value") );
    }

    public void assertPage( String title )
    {
        Assert.assertEquals( title,  getTitle());
    }

    public String getTitle()
    {
        // Collapse spaces
        return getWebDriver().getTitle().replaceAll( "[ \n\r]+", " " );
    }

    public String getHtmlContent()
    {
        return getWebDriver().getPageSource();
    }

    public String getText( String locator )
    {
        return findElement(locator ).getText();
    }

    public void assertTextPresent( String text )
    {
        Assert.assertTrue( "'" + text + "' isn't present.", getWebDriver().getPageSource().contains( text ) );
    }


    public void assertTextNotPresent( String text )
    {
        Assert.assertFalse( "'" + text + "' is present.", isTextPresent( text ) );
    }

    public void assertElementPresent( String elementLocator )
    {
        Assert.assertTrue( "'" + elementLocator + "' isn't present.", isElementPresent( elementLocator ) );
    }

    public void assertElementNotPresent( String elementLocator )
    {
        Assert.assertFalse( "'" + elementLocator + "' is present.", isElementPresent( elementLocator ) );
    }

    public void assertLinkPresent( String text )
    {
        Assert.assertTrue( "The link '" + text + "' isn't present.", isElementPresent( "//*[text()='" + text+"']//ancestor::a"  ) );
    }

    public void assertLinkNotPresent( String text )
    {
        Assert.assertFalse( "The link('" + text + "' is present.", isElementPresent( "//*[text()='" + text+"']//ancestor::a" ) );
    }

    public void assertLinkNotVisible( String text )
    {
        Assert.assertFalse( "The link('" + text + "' is visible.", isElementVisible( "//*[text()='" + text+"']//ancestor::a"  ) );
    }

    public void assertLinkVisible( String text )
    {
        Assert.assertTrue( "The link('" + text + "' is not visible.", isElementVisible( "//*[text()='" + text+"']//ancestor::a" ) );
    }

    public void assertImgWithAlt( String alt )
    {
        assertElementPresent( "/¯img[@alt='" + alt + "']" );
    }

    public void assertImgWithAltAtRowCol( boolean isALink, String alt, int row, int column )
    {
        String locator = "//tr[" + row + "]/td[" + column + "]/";
        locator += isALink ? "a/" : "";
        locator += "img[@alt='" + alt + "']";

        assertElementPresent( locator );
    }

    public void assertImgWithAltNotPresent( String alt )
    {
        assertElementNotPresent( "/¯img[@alt='" + alt + "']" );
    }



    public boolean isTextPresent( String text )
    {
        return getWebDriver().getPageSource().contains(text);
    }

    public boolean isLinkPresent( String text )
    {
        return isElementPresent( "//*[text()='" + text +"']//ancestor::a" );
    }

    public boolean isElementPresent( String locator )
    {
        try
        {
            return findElement(locator ) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementVisible(String locator )
    {
        try {
            return findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }


    public void waitPage()
    {
        // TODO define a smaller maxWaitTimeJsInMs for wait javascript response for browser side validation
        //getSelenium().w .wait( Long.parseLong( maxWaitTimeInMs ) );
        //getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        // http://jira.openqa.org/browse/SRC-302
        // those hack looks to break some tests :-(
        // getSelenium().waitForCondition( "selenium.isElementPresent('document.body');", maxWaitTimeInMs );
        //getSelenium().waitForCondition( "selenium.isElementPresent('footer');", maxWaitTimeInMs );
        //getSelenium().waitForCondition( "selenium.browserbot.getCurrentWindow().document.getElementById('footer')",
        //                                maxWaitTimeInMs );
        // so the only hack is to not use a too small wait time

        try
        {
            Thread.sleep( maxWaitTimeInMs );
        }
        catch ( InterruptedException e )
        {
            throw new RuntimeException( "issue on Thread.sleep : " + e.getMessage(), e );
        }
    }

    public String getFieldValue( String fieldName )
    {
        return findElement(fieldName ).getAttribute( "value" );
    }


    public void selectValue( String locator, String value )
    {
        WebElement element = findElement(locator );
        Select select = new Select(element);
        select.selectByValue( value );
    }

    public WebElement findElement(String locator) {
        if (locator.startsWith("/")) {
            return getWebDriver().findElement( By.xpath( locator ) );
        } else {
            return getWebDriver().findElement( By.id(locator) );
        }
    }


    public void submit()
    {
        clickLinkWithXPath( "//input[@type='submit']" );
    }

    public void assertButtonWithValuePresent( String text )
    {
        Assert.assertTrue( "'" + text + "' button isn't present", isButtonWithValuePresent( text ) );
    }

    public void assertButtonWithIdPresent( String id )
    {
        Assert.assertTrue( "'Button with id =" + id + "' isn't present", isButtonWithIdPresent( id ) );
    }

    public void assertButtonWithValueNotPresent( String text )
    {
        Assert.assertFalse( "'" + text + "' button is present", isButtonWithValuePresent( text ) );
    }

    public boolean isButtonWithValuePresent( String text )
    {
        return isElementPresent( "//button[@value='" + text + "']" ) || isElementPresent(
            "//input[@value='" + text + "']" );
    }

    public boolean isButtonWithIdPresent( String text )
    {
        return isElementPresent( "//button[@id='" + text + "']" ) || isElementPresent( "//input[@id='" + text + "']" );
    }

    public void clickButtonWithName( String text, boolean wait )
    {
        clickLinkWithXPath( "//input[@name='" + text + "']", wait );
    }

    public void clickButtonWithValue( String text )
    {
        clickButtonWithValue( text, false );
    }

    public void clickButtonWithValue( String text, boolean wait )
    {
        assertButtonWithValuePresent( text );

        if ( isElementPresent( "//button[@value='" + text + "']" ) )
        {
            clickLinkWithXPath( "//button[@value='" + text + "']", wait );
        }
        else
        {
            clickLinkWithXPath( "//input[@value='" + text + "']", wait );
        }
    }

    public void clickSubmitWithLocator( String locator )
    {
        clickLinkWithLocator( locator );
    }

    public void clickSubmitWithLocator( String locator, boolean wait )
    {
        clickLinkWithLocator( locator, wait );
    }

    public void clickImgWithAlt( String alt )
    {
        clickLinkWithLocator( "//img[@alt='" + alt + "']" );
    }

    public void clickLinkWithText( String text )
    {
        clickLinkWithText( text, false );
    }

    public void clickLinkWithText( String text, boolean wait )
    {
        clickLinkWithLocator( "//*[text()='" + text +"']//ancestor::a", wait );
    }

    public void clickLinkWithXPath( String xpath )
    {
        clickLinkWithXPath( xpath, false );
    }

    public void clickLinkWithXPath( String xpath, boolean wait )
    {
        clickLinkWithLocator( xpath, wait );
    }

    public void clickLinkWithLocator( String locator )
    {
        clickLinkWithLocator( locator, false );
    }

    public void clickLinkWithLocator( String locator, boolean wait )
    {
        assertElementPresent( locator );
        findElement(locator).click();
        if ( wait )
        {
            waitPage();
        }
    }

    public void clickButtonWithLocator( String locator )
    {
        clickButtonWithLocator( locator, false );
    }

    public void clickButtonWithLocator( String locator, boolean wait )
    {
        assertElementPresent( locator );
        findElement(locator ).click();
        if ( wait )
        {
            waitPage();
        }
    }

    public <V> V tryClick(By clickableLocator, Function<? super WebDriver, V> conditions, String message, int attempts, int maxWaitTimeInS) {

        int count = attempts;
        WebDriverWait wait = new WebDriverWait( getWebDriver(), maxWaitTimeInS );
        V result = null;
        Exception ex = null;
        while(count>0)
        {
            try
            {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable( clickableLocator ));
                el.click();
                result = wait.until( conditions  );
                count=0;
                ex = null;
            } catch (Exception e) {
                ex = e;
                count--;
            }
        }
        if (ex!=null) {
            Assert.fail( message);
        }
        return result;
    }

    /**
     * Executes click() on the WebElement <code>el</code> and waits for the conditions.
     * If the condition is not fulfilled in <code>maxWaitTimeInS</code>, the click is executed again
     * and waits again for the condition.
     * After the number of attempts as given by the parameter an assertion error will be thrown, with
     * the given <code>message</code>.
     *
     * If the click was successful the element is returned that was created by the condition.
     *
     * @param el The element where the click is executed
     * @param conditions The conditions to wait for after the click
     * @param message The assertion messages
     * @param attempts Maximum number of click attempts
     * @param maxWaitTimeInS The time in seconds to wait that the condition is fulfilled.
     * @param <V> The return type
     * @return
     */
    public <V> V tryClick( WebElement el, Function<? super WebDriver, V> conditions, String message, int attempts, int maxWaitTimeInS)
    {
        int count = attempts;
        WebDriverWait wait = new WebDriverWait( getWebDriver(), maxWaitTimeInS );
        V result = null;
        Exception ex = null;
        while(count>0)
        {
            el.click();
            try
            {
                result = wait.until( conditions  );
                count=0;
                ex = null;
            } catch (Exception e) {
                ex = e;
                count--;
            }
        }
        if (ex!=null) {
            Assert.fail( message);
        }
        return result;
    }

    public <V> V tryClick(WebElement el, Function<? super WebDriver, V> conditions, String message, int attempts )
    {
        return tryClick( el, conditions, message, attempts, 10 );
    }

    public <V> V tryClick(WebElement el, Function<? super WebDriver, V> conditions, String message)
    {
        return tryClick( el, conditions, message, 3);
    }


    public void setFieldValues( Map<String, String> fieldMap )
    {
        Map.Entry<String, String> entry;

        for ( Iterator<Entry<String, String>> entries = fieldMap.entrySet().iterator(); entries.hasNext(); )
        {
            entry = entries.next();

            setFieldValue( entry.getKey(), entry.getValue() );
        }
    }

    public void setFieldValue( String fieldName, String value )
    {
        findElement(fieldName ).sendKeys( value );
    }

    public void checkField( String locator )
    {
        WebElement element = findElement(locator );
        if (!element.isSelected()) {
            element.click();
        }
    }

    public void uncheckField( String locator )
    {
        WebElement element = findElement(locator );
        if (element.isSelected()) {
            element.click();
        }
    }

    public boolean isChecked( String locator )
    {
        return findElement(locator ).isSelected();
    }

    public void assertIsChecked( String locator )
    {

        Assert.assertTrue( isChecked( locator ));
    }

    public void assertIsNotChecked( String locator )
    {

        Assert.assertFalse( isChecked( locator ) );
    }



    public String captureScreenShotOnFailure( Throwable failure, String methodName, String className )
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd-HH_mm_ss" );
        String time = sdf.format( new Date() );
        File targetPath = new File( "target", "screenshots" );

        int lineNumber = 0;

        for ( StackTraceElement stackTrace : failure.getStackTrace() )
        {
            if ( stackTrace.getClassName().equals( this.getClass().getName() ) )
            {
                lineNumber = stackTrace.getLineNumber();
                break;
            }
        }

        targetPath.mkdirs();
        if (getWebDriver()!=null)
        {
            String fileBaseName = methodName + "_" + className + ".java_" + lineNumber + "-" + time;
            File fileName = new File( targetPath, fileBaseName + ".png" );
            Path screenshot = WebdriverUtility.takeScreenShot( fileName.getName(), getWebDriver());
            return fileName.getAbsolutePath();
        } else {
            return "";
        }
    }

}