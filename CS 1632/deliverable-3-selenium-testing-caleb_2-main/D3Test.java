// Generated by Selenium IDE
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
public class D3Test {
  private WebDriver driver;
  private Map<String, Object> vars;
  JavascriptExecutor js;
  @Before
  public void setUp() {
	System.setProperty("webdriver.gecko.driver", "Firefox/geckodriver-mac-intel");
	System.setProperty("webdriver.firefox.logfile", "/dev/null");
    driver = new FirefoxDriver();
    js = (JavascriptExecutor) driver;
    vars = new HashMap<String, Object>();
  }
  @After
  public void tearDown() {
    driver.quit();
  }
  @Test
  public void tESTLINKS() {
    driver.get("https://cs1632.appspot.com/");
    driver.manage().window().setSize(new Dimension(1920, 1080));
    {
      WebElement element = driver.findElement(By.linkText("Reset"));
      String attribute = element.getAttribute("href");
      vars.put("reset_href", attribute);
    }
    assertEquals(vars.get("reset_href").toString(), "https://cs1632.appspot.com/reset");
  }
  @Test
  public void dEFECT1FUNGREETACAT() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.manage().window().setSize(new Dimension(1920, 1080));
    driver.findElement(By.linkText("Rent-A-Cat")).click();
    driver.findElement(By.id("rentID")).click();
    driver.findElement(By.id("rentID")).sendKeys("2");
    driver.findElement(By.xpath("//button[@onclick=\'rentSubmit()\']")).click();
    driver.findElement(By.linkText("Greet-A-Cat")).click();
    assertThat(driver.findElement(By.xpath("//div[@id=\'greeting\']/h4")).getText(), is("Meow!Meow!"));
    driver.close();
  }
  @Test
  public void dEFECT2FUNFEED() {
    driver.get("https://cs1632.appspot.com/");
    driver.manage().window().setSize(new Dimension(1920, 1080));
    driver.findElement(By.linkText("Reset")).click();
    driver.findElement(By.linkText("Feed-A-Cat")).click();
    driver.findElement(By.id("catnips")).click();
    driver.findElement(By.id("catnips")).sendKeys("0");
    driver.findElement(By.cssSelector(".btn")).click();
    assertThat(driver.findElement(By.id("feedResult")).getText(), is("Cat fight!"));
    driver.close();
  }
  @Test
  public void dEFECT3FUNGREETACATWITHNAME() {
    driver.get("https://cs1632.appspot.com/");
    driver.manage().window().setSize(new Dimension(1107, 675));
    driver.findElement(By.linkText("Reset")).click();
    driver.findElement(By.linkText("Rent-A-Cat")).click();
    driver.findElement(By.id("rentID")).click();
    driver.findElement(By.id("rentID")).sendKeys("1");
    driver.findElement(By.cssSelector(".form-group:nth-child(3) .btn")).click();
    driver.get("https://cs1632.appspot.com//greet-a-cat/Jennyanydots");
    assertThat(driver.findElement(By.cssSelector("#greeting > h4")).getText(), is("Jennyanydots is not here."));
    driver.close();
  }
  @Test
  public void tESTCATALOG() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.findElement(By.linkText("Catalog")).click();
    driver.manage().window().setSize(new Dimension(1920, 1080));
    {
      WebElement element = driver.findElement(By.xpath("//li[2]/img"));
      String attribute = element.getAttribute("src");
      vars.put("img", attribute);
    }
    assertEquals(vars.get("img").toString(), "https://cs1632.appspot.com/images/cat2.jpg");
    driver.close();
  }
  @Test
  public void tESTFEED() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.findElement(By.linkText("Feed-A-Cat")).click();
    driver.findElement(By.id("catnips")).click();
    driver.findElement(By.id("catnips")).sendKeys("6");
    driver.findElement(By.cssSelector(".btn")).click();
    assertThat(driver.findElement(By.xpath("//div[@id=\'feedResult\']")).getText(), is("Nom, nom, nom."));
    driver.close();
  }
  @Test
  public void tESTFEEDACAT() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.findElement(By.linkText("Feed-A-Cat")).click();
    {
      List<WebElement> elements = driver.findElements(By.xpath("//button[@onclick=\'feedSubmit()\']"));
      assert(elements.size() > 0);
    }
    driver.close();
  }
  @Test
  public void tESTGREETACAT() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Greet-A-Cat")).click();
    assertThat(driver.findElement(By.xpath("//div[@id=\'greeting\']/h4")).getText(), is("Meow!Meow!Meow!"));
    driver.close();
  }
  @Test
  public void tESTGREETACATWITHNAME() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.get("https://cs1632.appspot.com//greet-a-cat/Jennyanydots");
    assertThat(driver.findElement(By.xpath("//div[@id=\'greeting\']/h4")).getText(), is("Meow! from Jennyanydots."));
    driver.close();
  }
  @Test
  public void tESTLISTING() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.manage().window().setSize(new Dimension(1920, 1080));
    {
      List<WebElement> elements = driver.findElements(By.xpath("//div[@id=\'listing\']/ul/li[3]"));
      assert(elements.size() > 0);
    }
    {
      List<WebElement> elements = driver.findElements(By.xpath("//div[@id=\'listing\']/ul/li[4]"));
      assert(elements.size() == 0);
    }
    assertThat(driver.findElement(By.xpath("//li[contains(.,\'ID 3. Mistoffelees\')]")).getText(), is("ID 3. Mistoffelees"));
  }
  @Test
  public void tESTRENT() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.manage().window().setSize(new Dimension(947, 656));
    driver.findElement(By.linkText("Rent-A-Cat")).click();
    driver.findElement(By.id("rentID")).click();
    driver.findElement(By.id("rentID")).sendKeys("2");
    driver.findElement(By.cssSelector(".form-group:nth-child(3) .btn")).click();
    assertThat(driver.findElement(By.xpath("//div[@id=\'listing\']/ul/li[2]")).getText(), is("Rented out"));
    assertThat(driver.findElement(By.xpath("//div[@id=\'rentResult\']")).getText(), is("Success!"));
    driver.close();
  }
  @Test
  public void tESTRENTACAT() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.manage().window().setSize(new Dimension(1920, 1080));
    driver.findElement(By.linkText("Rent-A-Cat")).click();
    {
      List<WebElement> elements = driver.findElements(By.xpath("//button[contains(.,\'Rent\')]"));
      assert(elements.size() > 0);
    }
    {
      List<WebElement> elements = driver.findElements(By.xpath("//button[contains(.,\'Return\')]"));
      assert(elements.size() > 0);
    }
    driver.close();
  }
  @Test
  public void tESTRESET() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.findElement(By.linkText("Rent-A-Cat")).click();
    driver.findElement(By.id("rentID")).click();
    driver.findElement(By.id("rentID")).sendKeys("2");
    driver.findElement(By.cssSelector(".form-group:nth-child(3) .btn")).click();
    driver.findElement(By.linkText("Reset")).click();
    assertThat(driver.findElement(By.xpath("//div/ul/li")).getText(), is("ID 1. Jennyanydots"));
    assertThat(driver.findElement(By.xpath("//div/ul/li[2]")).getText(), is("ID 2. Old Deuteronomy"));
    assertThat(driver.findElement(By.xpath("//div/ul/li[3]")).getText(), is("ID 3. Mistoffelees"));
    driver.close();
  }
  @Test
  public void tESTRETURN() {
    driver.get("https://cs1632.appspot.com/");
    driver.findElement(By.linkText("Reset")).click();
    driver.manage().window().setSize(new Dimension(1920, 1080));
    driver.findElement(By.linkText("Rent-A-Cat")).click();
    driver.findElement(By.id("rentID")).click();
    driver.findElement(By.id("rentID")).sendKeys("2");
    driver.findElement(By.cssSelector(".form-group:nth-child(3) .btn")).click();
    driver.findElement(By.id("returnID")).click();
    driver.findElement(By.id("returnID")).sendKeys("2");
    driver.findElement(By.cssSelector(".form-group:nth-child(4) .btn")).click();
    assertThat(driver.findElement(By.cssSelector(".list-group-item:nth-child(2)")).getText(), is("ID 2. Old Deuteronomy"));
    assertThat(driver.findElement(By.xpath("//div[@id=\'returnResult\']")).getText(), is("Success!"));
    driver.close();
  }
}
