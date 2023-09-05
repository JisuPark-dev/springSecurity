# springSecurity
제로베이스 강의를 통해서 하는 openstack 사용한 spring security 실습


## OpenStackFilter

### attemptAuthentication
<details>
이 코드는 Spring Security 프레임워크 내에서 인증 절차를 직접 정의하는 attemptAuthentication 메서드의 오버라이드입니다.

메서드의 작동 원리를 단계별로 나눠서 설명하면 다음과 같습니다:

데이터 추출:

HttpServletRequest에서 username, password, domain 정보를 추출합니다. 이 정보는 일반적으로 로그인 폼에서 제출될 때 HTTP POST 요청에 포함됩니다.
데이터 검증 및 정리:

각각의 값(username, password, domain)이 null인지 확인하고, null이면 빈 문자열로 초기화합니다.
username은 추가적으로 trim()을 사용하여 앞뒤의 공백을 제거합니다.
인증 객체 생성:

위에서 추출하고 정리한 username과 password 정보를 사용하여 UsernamePasswordAuthenticationToken 객체를 생성합니다. 이 객체는 Spring Security에서 인증 요청을 나타내는 표준 토큰 중 하나입니다.
usernamePasswordAuthenticationToken.setDetails(domain)을 사용하여 domain 정보를 해당 토큰의 세부 정보로 설정합니다. 이는 추가적인 인증 정보나 컨텍스트로 사용될 수 있습니다.
인증 절차:

this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken)을 호출하여 실제 인증을 수행합니다. 이때 사용되는 AuthenticationManager는 Spring Security의 주요 구성 요소 중 하나로, 제공된 인증 토큰의 유효성을 검증하고 필요한 경우 인증 세부 정보와 권한을 포함하는 새로운 인증 객체를 반환합니다.
간단히 요약하면, 이 메서드는 HTTP 요청에서 사용자 이름, 비밀번호, 도메인 정보를 추출하여 Spring Security 인증 메커니즘을 통해 인증을 시도합니다.
</details>

### checkExpire
<details>
Date 객체의 after 메서드를 사용하여 tokenExpireDateTime이 현재 시점 (new Date())보다 미래인지 확인합니다. 만약 미래라면 true를 반환하고, 그렇지 않으면 false를 반환합니다.
</details>

### doFilter
<details>
이 코드는 doFilter 메서드를 오버라이드하는데 사용됩니다. doFilter는 자바의 서블릿 필터에서 요청과 응답을 사전 및 사후 처리하는 로직을 정의하는 메서드입니다. 이 메서드는 주로 보안, 로깅, 트랜잭션 처리 등의 공통 작업을 수행하는 데 사용됩니다.

해당 코드는 크게 다음의 작업을 수행합니다:

ServletRequest와 ServletResponse를 캐스팅:
주어진 ServletRequest와 ServletResponse를 HttpServletRequest와 HttpServletResponse로 강제 형변환합니다.

인증 필요 여부 확인:
requiresAuthentication 메서드로 해당 요청이 인증을 필요로 하는지 확인합니다. 필요하지 않다면 필터 체인을 계속 진행하고 메서드를 종료합니다.

세션에서 토큰 정보 가져오기:
사용자의 세션에서 unscopedTokenId와 tokenExpired 속성값을 가져옵니다.

토큰 유효성 검사:

세션에서 토큰 정보를 성공적으로 가져왔다면, 해당 토큰의 만료 시간을 검사합니다.
checkExpire 메서드를 통해 토큰의 만료 시간이 현재 시간 이후인지 확인합니다. 만료되지 않았다면 필터 체인을 계속 진행합니다.
만약 토큰이 만료되었거나 오류가 발생했다면, unsuccessfulAuthentication 메서드를 호출하여 실패 처리를 합니다.
토큰이 없는 경우의 인증 절차:

현재 요청이 POST 요청이고 토큰이 세션에 없다면, 요청에서 username, password, domain 정보를 추출합니다.
유효한 인증 정보가 있다면 attemptAuthentication 메서드를 호출하여 인증을 시도합니다.
인증이 성공하면 sessionAuthenticationStrategy.onAuthentication과 successfulAuthentication 메서드를 호출하여 세션과 응답을 설정합니다.
만약 인증 중 오류가 발생하면, unsuccessfulAuthentication 메서드를 호출하여 실패 처리를 합니다.
기타 경우의 처리:

POST 요청이 아니거나 필요한 인증 정보가 없는 경우에도 unsuccessfulAuthentication 메서드를 호출하여 "Bye bye." 메시지와 함께 실패 처리를 합니다.
결론적으로, 이 doFilter 메서드는 주어진 HTTP 요청에 대한 인증 절차를 수행하며, 인증의 성공 여부에 따라 필요한 처리를 수행합니다.
</details>
---

## OpenStackAuth

<details>
이 코드는 OpenStack cloud computing platform의 인증 프로세스를 추상화하기 위한 OpenStackAuth라는 클래스를 정의하고 있습니다. OpenStack은 클라우드 인프라를 제공하기 위한 오픈 소스 소프트웨어 플랫폼입니다. 해당 코드에서는 OpenStack4j라는 자바 라이브러리를 사용하여 OpenStack API와의 상호 작용을 수행합니다.

각 코드 부분의 설명은 다음과 같습니다:

클래스 필드:

osClient: OpenStack API와 상호 작용하기 위한 클라이언트입니다.
token: 인증에 성공한 후 반환되는 토큰입니다.
tokenId: 해당 토큰의 ID입니다.
Getter 및 Setter 메서드:

getToken과 setToken: 토큰 객체에 대한 getter 및 setter 메서드입니다.
unscopedAuth 메서드:

현재는 정의되지 않은 static 메서드입니다. 이 메서드의 목적은 주어진 username, password, domain을 사용하여 unscoped 인증 객체를 생성하는 것으로 보입니다.
생성자 메서드들:

첫 번째 생성자: username, password, domain을 인자로 받아 OpenStack 인증을 수행합니다. OSFactory.builderV3().endpoint("") 부분에서는 endpoint URL이 아직 정의되지 않았음을 알 수 있습니다. 인증에 필요한 정보가 제공되면 해당 클라이언트를 사용하여 인증을 수행하고 결과 토큰을 저장합니다.
두 번째 생성자: username 및 password만 인자로 받아 첫 번째 생성자를 호출하는 오버로드된 생성자입니다. domain은 null로 설정되며, 첫 번째 생성자에서 이를 처리합니다.
이 클래스는 OpenStack과의 인증을 추상화하며, 주어진 사용자 이름, 비밀번호, 도메인을 사용하여 인증을 수행하고 결과 토큰을 저장합니다.
</details>

### OpenStackAuth(String tokenId)
<details>
이 코드는 OpenStackAuth라는 클래스의 생성자를 정의하고 있습니다. 이 생성자는 OpenStack 클라우드 플랫폼의 인증 절차와 관련이 있습니다. 주어진 tokenId를 이용하여 OpenStack API에 인증을 시도하는 로직을 수행합니다. 코드의 주요 부분들을 상세하게 살펴보겠습니다.

OpenStack Client 설정:

java
Copy code
IOSClientBuilder.V3 v3 = OSFactory.builderV3()
.endpoint(Constants.OPENSTACK_KEYSTONE_URL + Constants.KEYSTONE_API_VERSION)
.token(tokenId);
OSFactory.builderV3()를 통해 OpenStack API V3 버전에 대한 클라이언트 빌더를 생성합니다.
.endpoint(Constants.OPENSTACK_KEYSTONE_URL + Constants.KEYSTONE_API_VERSION)를 사용하여 인증 서비스의 URL을 설정합니다. 이 URL은 상수로 미리 정의된 OPENSTACK_KEYSTONE_URL와 KEYSTONE_API_VERSION을 결합하여 생성됩니다.
.token(tokenId)를 통해 주어진 tokenId를 사용하여 인증을 시도할 것임을 지정합니다.
인증 시도:

java
Copy code
this.osClient = v3.authenticate();
위에서 설정한 클라이언트 빌더를 통해 실제 인증을 시도합니다. 성공적으로 인증이 수행되면 osClient 객체에 인증된 클라이언트 세션 정보가 저장됩니다.
Token 정보 저장:

java
Copy code
this.token = this.osClient.getToken();
this.tokenId = this.token.getId();
this.osClient.getToken()을 통해 인증된 토큰 정보를 가져와서 token 멤버 변수에 저장합니다.
그리고 this.token.getId()를 통해 토큰의 ID 값을 가져와 tokenId 멤버 변수에 저장합니다.
간단하게 요약하면, 이 생성자는 주어진 tokenId를 사용하여 OpenStack에 인증을 시도하고, 인증 성공 시 관련 토큰 정보를 내부 변수에 저장하는 작업을 수행합니다
</details>