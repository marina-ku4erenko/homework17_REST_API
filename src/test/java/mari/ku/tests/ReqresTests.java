package mari.ku.tests;

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
import static org.assertj.core.api.Assertions.assertThat;

public class ReqresTests {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://reqres.in";
    }

    //1) Успешная регистрация пользователя https://reqres.in/api/register; проверить, что id и token не null
    @DisplayName("Проверка успешной регистрации пользователя")
    @Test
    void checkSuccessfulRegister() {

        JSONObject requestBody = new JSONObject();
        requestBody.put("email", "eve.holt@reqres.in");
        requestBody.put("password", "cityslicka");

        Response responseBody =
                given()
                        .contentType(JSON)
                        .body(requestBody.toString())
                        .when()
                        .post("/api/register")
                        .then()
                        .statusCode(200)
                        .extract().response();

        JsonPath jsonPathResponseBody = responseBody.jsonPath();
        assertThat((Integer) jsonPathResponseBody.get("id")).as("id").isNotNull();
        assertThat((String) jsonPathResponseBody.get("token")).as("token").isNotNull();
    }

    //2) НЕуспешная регистрация пользователя https://reqres.in/api/register; проверить текст ошибки и статус код 400
    @DisplayName("Проверка текста ошибки при неуспешной регистрации пользователя")
    @Test
    void checkUnsuccessfulRegister() {

        JSONObject requestBody = new JSONObject();
        requestBody.put("email", "eve.holt@reqres.in");

        Response responseBody =
                given()
                        .contentType(JSON)
                        .body(requestBody.toString())
                        .when()
                        .post("/api/register")
                        .then()
                        .statusCode(400)
                        .extract().response();

        JsonPath jsonPathResponseBody = responseBody.jsonPath();
        assertThat((String) jsonPathResponseBody.get("error")).as("error").isEqualTo("Missing password");
    }

    //3) От реста https://reqres.in/api/unknown получить инфу, сколько сего цветов в списке ("total": 12) + проверить статус код 200;
    //с помощью реста https://reqres.in/api/unknown/{id} проверить в каждом, что все поля заполнены
    @DisplayName("Проверка заполненности полей для каждого из цветов")
    @Test
    void checkEachColor() {

        Response responseBodyList =
                get("/api/unknown")
                        .then()
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

    //4) Проверить, что не существует цвет c id https://reqres.in/api/unknown/{id}; проверить статус код 404
    @DisplayName("Проверка статус кода 404 при переходе к несуществующему цвету")
    @Test
    void checkUnknownColor404() {

        Response responseBodyList =
                get("/api/unknown")
                        .then()
                        .statusCode(200)
                        .extract().response();

        JsonPath jsonPathResponseBodyList = responseBodyList.jsonPath();
        Integer total = jsonPathResponseBodyList.get("total");
        assertThat(total).as("total").isNotNull();

        get("/api/unknown/" + (total + 1))
                .then()
                .statusCode(404);
    }

    //5) Изменить данные пользователя через PUT https://reqres.in/api/users/{id}; проверить, что updatedAt не null
    @DisplayName("Проверка успешного изменения данных пользователя через PUT запрос")
    @Test
    void checkUpdateUser() {

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "morpheus");
        requestBody.put("job", "zion resident");

        Response responseBody =
                given()
                        .contentType(JSON)
                        .body(requestBody.toString())
                        .when()
                        .put("/api/users/2")
                        .then()
                        .statusCode(200)
                        .extract().response();

        JsonPath jsonPathResponseBody = responseBody.jsonPath();
        assertThat((String) jsonPathResponseBody.get("name")).as("name").isEqualTo(requestBody.get("name"));
        assertThat((String) jsonPathResponseBody.get("job")).as("job").isEqualTo(requestBody.get("job"));
        assertThat((String) jsonPathResponseBody.get("updatedAt")).as("updatedAt").isNotNull();

    }
}

