import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class YandexMarket {
    protected static WebDriver driver;
    private static int timeout = 5;
    private static Logger logger = LogManager.getLogger(YandexMarket.class);

    private static void clickElementByLocator(By locator) {
        boolean noex = true;
        int tries = 0;
        while ( noex && tries < timeout ) {
            tries += 1;
            try {
                driver.findElement(locator).click();
                noex = false;
            } catch ( StaleElementReferenceException ser ) {
                noex = true;
                logger.info("StaleElementReferenceException found. Trying again.");
            }
        }
    }

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        logger.info("Драйвер поднят");
    }

    @Test
    public void Yandex() throws InterruptedException {
        String url = "https://market.yandex.ru/";
        String manufacturer1 = "HUAWEI";
        String manufacturer2 = "Xiaomi";
        String categories = "[data-zone-name='all-categories']";
        String phones = "[data-zone-name='menu'] [href*='catalog--mobilnye-telefony']";
        String manufacturerFull1 = "//span[contains(text(),'" + manufacturer1 + "')]";
        String manufacturerFull2 = "//span[contains(text(),'" + manufacturer2 + "')]";
        String price = "[data-autotest-id='dprice']";
        //Маркет после сортировки почему-то возвращает не только смартфоны
        //У меня в списке оказались дисплей и швабра
        String firstMan1 = "(//div[contains(text(),'" + manufacturer1 + "')]" +
                "/..//span[contains(text(), 'Смартфон')]" +
                "/../../../..//div[contains(@aria-label,'сравнению')]//div)[1]";
        String firstMan2 = "(//div[contains(text(),'" + manufacturer2 + "')]" +
                "/..//span[contains(text(), 'Смартфон')]" +
                "/../../../..//div[contains(@aria-label,'сравнению')]//div)[1]";
        String more = "[role='button']";
        String comparePopup = "//div[contains(text(),'добавлен')]/../..";
        String compareButton = "[href='/my/compare-lists']";
        String list = "[data-apiary-widget-name='@MarketNode/CompareContent'] div[style] div[style] a";
        int sizeExpected = 2;
        String properties = "//button[contains(text(), 'Все характеристики')]";
        String os = "//div[contains(text(), 'Операционная система')]";
        String diff = "//button[contains(text(), 'Различающиеся характеристики')]";
        driver.get(url);
        logger.info("Клик на \"Каталог товаров\"");
        driver.findElement(By.cssSelector(categories)).click();
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        logger.info("Подождать элемента \"Мобильные телефоны\"");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(phones)));
        logger.info("Клик на \"Мобильные телефоны\"");
        driver.findElement(By.cssSelector(phones)).click();
        logger.info("Подождать прогрузки списка");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(more)));

        logger.info("Выбрать производителя " + manufacturer1);
        driver.findElement(By.xpath(manufacturerFull1)).click();

        logger.info("Выбрать производителя " + manufacturer2);
        driver.findElement(By.xpath(manufacturerFull2)).click();

        logger.info("Сортировка по цене");
        driver.findElement(By.cssSelector(price)).click();

        logger.info("Подождать прогрузки списка после сортировки");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(more)));
        logger.info("Выбрать первый " + manufacturer1);
        //Даже после появления "Показать еще" click() иногда возвращал stale exception
        //Пришлось написать обработку
        clickElementByLocator(By.xpath(firstMan1));

        logger.info("Подождать плашки сравнения для " + manufacturer1);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(comparePopup)));

        logger.info("Выбрать первый " + manufacturer2);
        //clickElementByLocator(By.xpath(firstMan2));
        driver.findElement(By.xpath(firstMan2)).click();

        logger.info("Подождать плашки сравнения для " + manufacturer2);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(comparePopup)));

        logger.info("Клик на \"Сравнить\"");
        driver.findElement(By.cssSelector(compareButton)).click();

        logger.info("Проверка количества элементов");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(list), sizeExpected));

        logger.info("Клик на \"Все характеристики\"");
        driver.findElement(By.xpath(properties)).click();

        logger.info("Проверка видимости ОС");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(os)));

        logger.info("Клик на \"Различающиеся характеристики\"");
        driver.findElement(By.xpath(diff)).click();

        logger.info("Проверка невидимости ОС");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(os)));
    }

    @After
    public void setDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
