# MySpringBoot Framework

MySpringBoot은 Spring Boot를 모방하여 개발된 경량 웹 프레임워크로, 웹 애플리케이션 개발에 필요한 주요 기능을 제공합니다. 의존성 주입, 요청 처리, JSON 매핑 등 핵심 기능을 포함하며, Spring Boot의 동작 원리를 학습하고자 하는 개발자에게 적합합니다.

# 1. 사용 기술

- **Java 17**: 프레임워크 개발에 사용된 주요 언어
- **Jakarta Servlet API**: HTTP 요청 및 응답 처리
- **Jetty**: 내장 HTTP 서버
- **ByteBuddy**: AOP를 위한 메서드 인터셉트 및 프록시 생성
- **정규표현식**: URL 패턴 매칭 및 변수 추출

# 2. 주요 기능

## 1. **Application Context**

빈(Bean) 생명주기를 관리하며 의존성 주입을 처리합니다.

1. **Bean 스캔 및 등록**
    
    Application 클래스가 존재하는 디렉토리부터 하위 디렉토리에서 `@Component`을 메타 어노테이션으로 가지고 있는 어노테이션(예시: `@Repository`, `@Service`, `@RestController`)이나 `@Component`이 선언된 클래스를 스캔하고 Bean으로 등록합니다.
    
2. 의존성 주입
    
    `@Autowired` 애너테이션을 통해 객체 간의 의존성을 자동으로 주입합니다.
    

## 2. DispatcherServlet

`DispatcherServlet`은 MySpringBoot에서 요청과 응답의 흐름을 중앙에서 제어하는 핵심 컴포넌트입니다. 

1. **HTTP 요청 수신 및 분석**:
    
    클라이언트로부터 전달된 요청 URL, HTTP 메서드(GET, POST 등), 쿼리 파라미터 등을 파악합니다.
    
2. **HandlerMapping을 통한 핸들러 탐색**:
    
    요청 정보를 기반으로 `HandlerMapping`을 사용하여 요청을 처리할 핸들러(컨트롤러 메서드)를 찾습니다. (핸들러를 찾을 수 없을 경우 `404 Not Found` 응답을 반환합니다.)
    
3. **HandlerAdapter를 통한 핸들러 실행**:
    
    `HandlerAdapter`를 호출하여, 요청 데이터를 핸들러 메서드의 매개변수로 변환하고 실행합니다.
    
4. **응답 반환**:
    
    핸들러 메서드의 결과를 클라이언트로 반환합니다.
    
    JSON 형태로 직렬화하여 응답 본문에 포함합니다.
    

## 3. HandlerMapping

`HandlerMapping`은 요청 URL과 HTTP 메서드에 따라 알맞은 핸들러 메서드를 찾아주는 역할을 합니다. 내부적으로 URL 매핑을 위한 데이터 구조를 관리합니다.

1. **핸들러 등록**:
    
    `@RequestMapping` 또는 `@RequestMapping`을 메타 어노테이션으로 가진 어노테이션이 있는 컨트롤러 메서드를 등록합니다.
    
    요청 메서드(GET, POST 등)와 URL 패턴을 기반으로 핸들러를 매핑합니다.
    
    정규 표현식을 사용하여 URL 패턴과 요청 URL을 비교하고 변수(예: `/users/{id}`)를 추출합니다.
    
2. **핸들러 탐색**:
    
    클라이언트 요청 정보에 맞는 핸들러를 반환합니다.
    
    요청 메서드와 URL을 모두 일치시켜야 핸들러를 반환합니다.
    

## 4. HandlerAdapter

`HandlerAdapter`는 `HandlerMapping`이 찾아준 핸들러를 실행하고, 요청 데이터를 메서드 매개변수로 바인딩하는 역할을 합니다.

1. **요청 데이터 바인딩**:
    
    요청 URL, 쿼리 파라미터, HTTP Body 데이터를 읽어 메서드 매개변수에 맞게 변환합니다.
    
    `@PathVariable`, `@RequestParam`, `@RequestBody` 애너테이션을 지원합니다.
    
    요청 데이터를 분석하여 필요한 값을 추출하고 `ObjectMapper`를 사용해 변환합니다.
    
2. **핸들러 메서드 실행**:
    
    바인딩된 데이터를 사용해 핸들러 메서드를 호출하고, 결과를 반환합니다.
    
3. **응답 생성**:
    
    메서드 결과를 JSON으로 직렬화하고 HTTP 응답에 포함합니다.
    
    메서드 실행 후, `ResponseEntity`와 같은 결과 객체를 처리합니다.
    

## 5. JsonParser

`JsonParser`는 JSON 문자열을 Java 객체로 변환하거나 Java 객체를 JSON 문자열로 변환하는 기본 도구입니다.

1. JSON 데이터를 Java의 `Map`, `List`, `String`, `Number`, `Boolean`, `null` 등으로 변환
2. JSON 구문 오류 감지 및 처리

## 6. ObjectMapper

`ObjectMapper`는 JSON 데이터를 객체 지향적으로 다룰 수 있는 고급 도구로, `JsonParser`를 기반으로 동작합니다.

1. JSON 문자열을 사용자 정의 Java 객체로 변환
2. Java 객체를 JSON 문자열로 직렬화

## 7. **SpringApplication**

애플리케이션을 부트스트랩하여 `ApplicationContext`를 초기화하고, 내장 서버를 시작하며, 시작 배너를 출력합니다.

# 3. UML
## 1. 클래스 다이어그램
![image](https://github.com/user-attachments/assets/c0d15f7c-16b2-45fa-b51a-c07015420c6c)
## 2. 요청 처리 흐름
![image](https://github.com/user-attachments/assets/145602e6-e55f-4a8e-9fa1-2e1c578c555d)


# 3. 설치 및 실행

### 1. GitHub에서 프로젝트 클론

먼저 MySpringBoot 프로젝트를 클론합니다.

```bash
git clone https://github.com/mong3125/myspringboot.git
cd myspringboot
```

### 2. 빌드 및 실행

**의존성 설치 및 빌드**

아래 명령어를 사용하여 MySpringBoot를 빌드합니다.

빌드 결과로 모든 의존성이 포함된 JAR 파일이 생성됩니다.

```bash
./gradlew shadowJar
```

### 3. 생성된 jar 파일 이동

빌드된 JAR 파일은 `build/libs` 디렉터리에 생성됩니다.

이제 애플리케이션 프로젝트를 생성하고 프로젝트의 root 디렉토리에 libs 폴더를 생성한 뒤, libs 폴더에 jar 파일을 옮겨야합니다.

### 4. build.gradle 수정

build.gradle의 dependencies에 다음과 같은 코드를 추가합니다.

```groovy
implementation files('libs/myspringboot-1.0-SNAPSHOT-all.jar')
```

### 5. 애플리케이션 클래스 생성

`src/main/java/com/exmaple/DemoApplication.java`

애플리케이션을 시작하는 진입점을 생성합니다.

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### 6. 컨트롤러 작성

`src/main/java/com/exmaple/controller/DemoController.java`

컨트롤러를 작성하여 간단한 엔드포인트를 추가합니다.

```java
@RestController
public class DemoController {

    @RequestMapping(method = RequestMethod.GET, value = "/hello")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello, World!", HttpStatusCode.OK);
    }
}
```

### 7. 실행 확인

애플리케이션을 실행한 후 브라우저 또는 cURL을 통해 동작을 확인합니다.

- **URL**: http://localhost:8080/hello
- **응답**:
    
    ```json
    {
        "message": "Hello, MySpringBoot!"
    }
    ```
