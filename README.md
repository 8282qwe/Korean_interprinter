## 언어 인터프린터 개발 보고서

### 목차

#####  1.언어인터프린터를만들게된배경

#####  2. 인터프린터분석

#####  3. 인터프린터설계

#####  4. 인터프린터사용설명

#####  5. 제작된인터프린터사용예시

#####  6. 제작된인터프린터에대한나의의견


### 1. 배경

#####  처음에 교수님께서 자신만의 언어인터프린터를 만들어보라는

##### 말씀에 막막함이 몰려왔다. 그래도 무언가를 만들어볼 수 있을거

##### 같다는생각에흥미를느껴제작하기로마음을먹었습니다.

##### 막상 언어에 대해 생각하다 보니 현재 대부분의 프로그래밍언어들이

##### 영어로만 이루어져 있다고 생각이 들었다. 그래서 이번에

##### 한번 “한글로 프로그래밍언어를 구현할 수 있으면어떨까?”

##### 라는 생각에서 제작을 시작하게 되었습니다.


### 2. 설계

 1.출력문(ex. C언어printf) : (변수)를출력

 2.입력문(ex.c언어scanf) : (변수)에입력값을저장

 3. 조건분기문(ex.c언어if) : 만약(조건문)이면(실행문)을, 아니면(실행문)을실행

 4. 무조건분기문(ex.c언어goto) : (함수이름)으로이동

 5. 반복문(ex.c언어for) : (횟수)만큼(실행문)을반복

 6. 함수선언문(ex.c언어void main()) : (함수이름)을선언

 7. 복귀문(ex.c언어return) : 복귀

 8. 종료문(메인함수의종료를뜻함): 종료

 9. 할당문(ex.c언어intI = 0;) : (변수이름)에(할당)을할당


### 3. BNF분석

```
 1. program -> declaration-list
 2. declaration-list -> declaration-list declaration | declaration
 3. declaration -> fun-declaration
 4. fun-declaration -> (Funtion_label) 을선언compound-stmt| (Function_label) 를선언compound-stmt
 5. compound-stmt-> statement-list
 6. statement-list -> statement-list statement | empty
 7. statement -> print-stmt| input stmt| selection-stmt| goto-stmt| for-stmt| expression-stmt| 끝| 복귀
 8.print-stmt -> (args) 를출력
 9.input-stmt -> (var) 에입력값을저장
 10. selection-stmt-> 만약(simple-expression) 이면(statement) 을, 아니면(statement) 을실행
 11. goto-stmt-> (Funtion_label)으로이동
 12. for-stmt-> (NUM) 만큼(statement)을반복
 13. expression-stmt-> (var)에(simple-expression)을할당
 14. simple-expression -> additive-expression relopadditive-expression | additive-expression
 15. relop-> <= | < | > | >= | = | !=
 16. addititve-expression -> addititve-expression addopterm | term
 17. addop-> + | -
 18. term -> term mulopfactor | factor
 19. mulop-> * | /
 20. factor -> var| NUM
 21. args-> var| Quote | NUM
```
```
 keywords : 를출력, 에입력값을저장, 만약이면을, 아니면을실행, 으로이동, 만큼을반복, 을선언, 에을할당, 복귀, 끝
 NUM = digit
 var= a | ... | z
 Quote = "인용구"
 Function_label= "함수이름"
```

### 4. 인터프린터사용설명
 같은 폴더에 test.txt란 파일을 생성하여 그안에 한글 명령어 작성 후 해당 인터프린터 실행


### 5.제작된인터프린터사용예시


### 6. 의견

#####  인터프린터를 설계하는 것이 쉬운 일이 아니라는 것을 깨달았습니다.

##### 한글이 좀 더 사람에게는 편하게 다가올 수 있지만 컴퓨터 입장에서

##### 봤을때는고려해야 될 사항이 많아져서 읽기 어려웠던거 같습니다.

##### 하지만 이번 프로젝트를 함에 있어서 Java로

##### 개발을 진행하다보니 Java에 대한 숙련도가 크게 상승하는

##### 계기가 되었고 좀 더 인터프린터에 대해 알아갈 수 있었던거 같았다.



