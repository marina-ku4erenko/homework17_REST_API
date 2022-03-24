package mari.ku.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static mari.ku.helpers.CustomAllureListener.withCustomTemplates;
import static org.assertj.core.api.Assertions.assertThat;

public class ReqresTests {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://reqres.in";
    }

    @DisplayName("Проверка успешной регистрации пользователя")
    @Test
    void checkSuccessfulRegister() {

        Allure.descriptionHtml("<p><span style=\"color: #008000;\">Отчёт с использованием<strong> AllureListener</strong></span></p>");

        JSONObject requestBody = new JSONObject();
        requestBody.put("email", "eve.holt@reqres.in");
        requestBody.put("password", "cityslicka");

        Response responseBody =
                given()
                        .filter(new AllureRestAssured())
                        .contentType(JSON)
                        .body(requestBody.toString())
                        .log().uri()
                        .log().body()
                        .when()
                        .post("/api/register")
                        .then()
                        .log().status()
                        .log().body()
                        .statusCode(200)
                        .extract().response();

        JsonPath jsonPathResponseBody = responseBody.jsonPath();
        assertThat((Integer) jsonPathResponseBody.get("id")).as("id").isNotNull();
        assertThat((String) jsonPathResponseBody.get("token")).as("token").isNotNull();
    }

    @DisplayName("Проверка текста ошибки при неуспешной регистрации пользователя")
    @Test
    void checkUnsuccessfulRegister() {

        Allure.descriptionHtml("<p><span style=\"color: #008000;\">Отчёт с использованием<strong> CustomAllureListener</strong></span></p>");

        JSONObject requestBody = new JSONObject();
        requestBody.put("email", "eve.holt@reqres.in");

        Response responseBody =
                given()
                        .filter(withCustomTemplates())
                        .contentType(JSON)
                        .body(requestBody.toString())
                        .log().uri()
                        .log().body()
                        .when()
                        .post("/api/register")
                        .then()
                        .log().status()
                        .log().body()
                        .statusCode(400)
                        .extract().response();

        JsonPath jsonPathResponseBody = responseBody.jsonPath();
        assertThat((String) jsonPathResponseBody.get("error")).as("error").isEqualTo("Missing password");
    }

    @DisplayName("Проверка заполненности полей для каждого из цветов")
    @Test
    void checkEachColor() {

        Allure.descriptionHtml("<p><span style=\"color: #008000;\">Отчёт с использованием<strong> CustomAllureListener</strong></span></p>");

        Response responseBodyList =
                given()
                        .filter(withCustomTemplates())
                        .log().uri()
                        .log().body()
                        .when()
                        .get("/api/unknown")
                        .then()
                        .log().status()
                        .log().body()
                        .statusCode(200)
                        .extract().response();

        JsonPath jsonPathResponseBodyList = responseBodyList.jsonPath();
        Integer total = jsonPathResponseBodyList.get("total");
        assertThat(total).as("total").isNotNull();

        for (int i = 1; i <= total; i++) {
            Response responseBodySingle =
                    get("/api/unknown/" + total)
                            .then()
                            .statusCode(200)
                            .extract().response();

            JsonPath jsonPathResponseBodySingle = responseBodySingle.jsonPath();
            assertThat((Integer) jsonPathResponseBodySingle.get("data.id")).as("data.id").isNotNull();
            assertThat((String) jsonPathResponseBodySingle.get("data.name")).as("data.name").isNotNull();
            assertThat((Integer) jsonPathResponseBodySingle.get("data.year")).as("data.year").isNotNull();
            assertThat((String) jsonPathResponseBodySingle.get("data.color")).as("data.color").isNotNull();
            assertThat((String) jsonPathResponseBodySingle.get("data.pantone_value")).as("data.pantone_value").isNotNull();
        }
    }

    @DisplayName("Проверка статус кода 404 при переходе к несуществующему цвету")
    @Test
    void checkUnknownColor404() {

        Allure.descriptionHtml("<p><span style=\"color: #008000;\">Отчёт с использованием<strong> AllureListener</strong></span></p>");

        Response responseBodyList =
                given()
                        .filter(new AllureRestAssured())
                        .log().uri()
                        .log().body()
                        .when()
                        .get("/api/unknown")
                        .then()
                        .log().status()
                        .log().body()
                        .statusCode(200)
                        .extract().response();

        JsonPath jsonPathResponseBodyList = responseBodyList.jsonPath();
        Integer total = jsonPathResponseBodyList.get("total");
        assertThat(total).as("total").isNotNull();

        get("/api/unknown/" + (total + 1))
                .then()
                .statusCode(404);
    }

    @DisplayName("Проверка успешного изменения данных пользователя с помощью PUT запроса")
    @Test
    void checkUpdateUser() {

        Allure.descriptionHtml("<p><span style=\"color: #008000;\">Отчёт с использованием<strong> CustomAllureListener</strong></span></p>");

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "morpheus");
        requestBody.put("job", "zion resident");

        Response responseBody =
                given()
                        .filter(withCustomTemplates())
                        .contentType(JSON)
                        .body(requestBody.toString())
                        .log().uri()
                        .log().body()
                        .when()
                        .put("/api/users/2")
                        .then()
                        .log().status()
                        .log().body()
                        .statusCode(200)
                        .extract().response();

        JsonPath jsonPathResponseBody = responseBody.jsonPath();
        assertThat((String) jsonPathResponseBody.get("name")).as("name").isEqualTo(requestBody.get("name"));
        assertThat((String) jsonPathResponseBody.get("job")).as("job").isEqualTo(requestBody.get("job"));
        assertThat((String) jsonPathResponseBody.get("updatedAt")).as("updatedAt").isNotNull();

    }
}

