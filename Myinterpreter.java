import java.io.*;
import java.util.*;

class InterpreterException extends Exception {
    String errStr; // describes the error
    public InterpreterException(String str) {
        errStr = str;
    }
    public String toString() {
        return errStr;
    }
}

class interpreter {
    private static final int PROG_SIZE = 10000;
    private char[] prog; // 프로시저에 대한 배열
    private int progIdx; // 프로그램안에서의 인덱스
    private int progIdx_K; //프레임 구할 때 쓰는 인덱스
    private int progIdx_F; //토큰 구할 때 쓰는 인덱스
    private String kwToken;
    private int kwTktype;
    private String Token;
    private int TokenType;
    private List<Character> Frame;
    private final TreeMap<String,Integer> fun_label;
    private final TreeMap<String,Integer> var_label = new TreeMap<>();

    class Keyword {
        String keyword; // 문자열 형태
        int keywordTok; // 내부 표현
        public Keyword(String str, int t) {
            keyword = str;
            keywordTok = t;
        }
    }

    final int UNKNCOM = 0;
    final int PRINT = 1;
    final int INPUT = 2;
    final int IF = 3;
    final int GOTO = 4;
    final int FOR = 5;
    final int DEF = 6;
    final int RETURN = 7;
    final int END = 8;
    final int ASM = 9;

    final int NONE = 0;
    final int VARIABLE = 1;
    final int COMMAND = 2;
    final int NUMBER = 3;
    final int DELIMITER = 4;
    final int QUOTEDSTR = 5;
    // 이중연산자 정의(ex. <=,>=,!=)
    final char LE = 1;
    final char GE = 2;
    final char NE = 3;
    // 관계연산자 정의
    char rops[] = {
            GE, NE, LE, '<', '>', '=', 0
    };
    /* 관계연산자를 좀더 편하게 계산하기 위한 문자열 클래스 */
    String relops = new String(rops);
    final Stack<Integer> fun_stack = new Stack<>();

    Keyword[] kwtable = {
            new Keyword("출력", PRINT),
            new Keyword("저장", INPUT),
            new Keyword("실행", IF),
            new Keyword("이동", GOTO),
            new Keyword("반복", FOR),
            new Keyword("선언", DEF),
            new Keyword("복귀", RETURN),
            new Keyword("종료", END),
            new Keyword("할당", ASM)
    };

    public interpreter(String progName) throws InterpreterException {
        char tempbuf[] = new char[PROG_SIZE];
        int size;
        fun_label = new TreeMap<>();
        // 프로그램을 실행하기위해 프로그램을 메모리에 읽어 올림
        size = loadProgram(tempbuf, progName);
        if (size != -1) {
            progIdx = 0;
            Frame = new ArrayList<>();
            // 프로그램을 저장할 적당한 크기의 배열 생성
            prog = new char[size+1];
            // 프로그램을 프로그램 배열로 복사
            System.arraycopy(tempbuf, 0, prog, 0, size);
            prog[size] = '\0';
        }
    }

    private int loadProgram(char[] p, String fname) throws InterpreterException {
        int size = 0;
        try {
            FileReader fr = new FileReader(fname);
            BufferedReader br = new BufferedReader(fr);
            size = br.read(p, 0, PROG_SIZE);
            fr.close();
        } catch (IOException exc) {
            handleErr(0);
        }
        // 파일이 EOF 기호로 끝나는 경우, 크기는 1만큼 감소시킴
        if (p[size - 1] == (char)26 ) size--;
        return size; // return size of program
    }

    private void scanLabels() throws InterpreterException {
        do {
            getKeyword();
            if(kwTktype == DEF){
                getFrame();
                getToken();
                fun_label.put(Token,progIdx-1);
            }
        }
        while(prog[progIdx] != '\0');
        progIdx = 0;
        progIdx_K = 0;
    }

    private void getKeyword() throws InterpreterException
    {
        keyword_init();
        int i = progIdx;
        while(prog[progIdx] != '\r' && prog[progIdx] != '\0')
            progIdx++;
        kwToken = prog[progIdx - 2] + Character.toString(prog[progIdx-1]);
        kwTktype = lookup(kwToken);
        progIdx_K = i;
        if(prog[progIdx] == '\0')
            return;
        progIdx+=2;
    }

    public void run() throws InterpreterException {
        // 새 프로그램을 위한 초기화
        scanLabels();
        mainstart();
    }

    private void mainstart() throws InterpreterException{
        do{
            getKeyword();
            switch(kwTktype){
                case PRINT:
                    print();
                    break;
                case INPUT:
                    input();
                    break;
                case IF:
                    fun_if();
                    break;
                case GOTO:
                    fun_goto();
                    break;
                case FOR:
                    fun_for();
                    break;
                case ASM:
                    assignment();
                    break;
                case RETURN:
                    fun_return();
                    break;
                case END:
                    return;
            }
        }while(prog[progIdx] != '\0');
    }

    private void getFrame() throws InterpreterException{
        Frame.clear();
        while(prog[progIdx_K] != '('){
            progIdx_K++;
        }
        progIdx_F = 0;
        int count = 1;
        progIdx_K++;
        while(count != 0){
            if (prog[progIdx_K] == '('){
                Frame.add(prog[progIdx_K]);
                progIdx_K++;
                count++;
            }
            else if (prog[progIdx_K] == ')'){
                Frame.add(prog[progIdx_K]);
                progIdx_K++;
                count--;
            }
            else{
                Frame.add(prog[progIdx_K]);
                progIdx_K++;
            }
        }
        Frame.remove(Frame.size()-1);
    }

    private void getToken() throws InterpreterException{
        TokenType = NONE;
        Token = "";

        if(progIdx_F == Frame.size()) {
            Token = "\r";
            return;
        }

        while(progIdx_F < Frame.size() && isSpaceOrTab(Frame.get(progIdx_F)))
            progIdx_F++;


        if(isDelim(Frame.get(progIdx_F))) {
            // 연산자인 경우
            Token += Frame.get(progIdx_F);
            progIdx_F++;
            TokenType = DELIMITER;
        }
        else if(Character.isLetter(Frame.get(progIdx_F))) {
            // 변수 또는 키워드인 경우
            while(!isDelim(Frame.get(progIdx_F))) {
                Token += Frame.get(progIdx_F);
                progIdx_F++;
                if(progIdx_F >= Frame.size()) break;
            }
            if(lookup(Token)==UNKNCOM) TokenType = VARIABLE;
        }
        else if(Character.isDigit(Frame.get(progIdx_F))) {
            // Is a number.
            while(!isDelim(Frame.get(progIdx_F))) {
                Token += Frame.get(progIdx_F);
                progIdx_F++;
                if(progIdx_F >= Frame.size())
                    break;
            }
            TokenType = NUMBER;
        }
        else if(Frame.get(progIdx_F) == '"') {
            // 인용부호가 있는 문자열인 경우
            progIdx_F++;
            char ch = Frame.get(progIdx_F);
            while(ch !='"' && ch != '\r') {
                Token += ch;
                progIdx_F++;
                ch = Frame.get(progIdx_F);
            }
            if(ch == '\r') handleErr(5);
            progIdx++;
            TokenType = QUOTEDSTR;
        }
        else if(Frame.get(progIdx_F) == '<' || Frame.get(progIdx_F) == '>') {
            if(progIdx_F+1 == Frame.size()) handleErr(6);
            switch(Frame.get(progIdx_F)) {
                case '<':
                    if(Frame.get(progIdx_F+1) == '>') {
                        progIdx_F += 2;
                        Token = String.valueOf(NE);
                    }
                    else if(Frame.get(progIdx_F+1) == '=') {
                        progIdx_F += 2;
                        Token = String.valueOf(LE);
                    }
                    else {
                        progIdx_F++;
                        Token = "<";
                    }
                    break;
                case '>':
                    if(Frame.get(progIdx_F+1) == '=') {
                        progIdx_F += 2;;
                        Token = String.valueOf(GE);
                    }
                    else {
                        progIdx_F++;
                        Token = ">";
                    }
                    break;
            }
            TokenType = DELIMITER;
            return;
        }
    }

    private void assignment() throws InterpreterException{
        char buffer;

        getFrame();
        getToken();
        buffer = Token.charAt(0);
        if(!Character.isLetter(buffer)) {
            handleErr(1);
            return;
        }
        String var = Token;
        getFrame();
        double result = evaluate();
        var_label.put(var,(int)result);
    }

    private void input() throws InterpreterException{
        String str;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        getFrame();
        getToken();
        System.out.print("input: ");

        try {
            str = br.readLine();
            var_label.put(Token,Integer.valueOf(str)); // read the value
        } catch (IOException exc) {
            handleErr(4);
        } catch (NumberFormatException exc) {
            System.out.println("Invalid input.");
        }
    }

    private void fun_goto() throws InterpreterException{
        getFrame();
        getToken();
        fun_stack.push(progIdx-1);
        fun_stack.push(progIdx_K);
        if(fun_label.get(Token) != null){
            int buf = fun_label.get(Token);
            progIdx = buf;
            progIdx_K = buf;
        }
    }

    private void fun_for() throws InterpreterException{
        getFrame();
        getToken();
        getFrame();
        Frame.add('\0');
        int progIdx_b = progIdx;
        int progIdx_fb = progIdx_F;
        int progIdx_kb = progIdx_K;
        char[] prog_b = prog;
        for(int i = 0; i < Frame.size(); i++){
            prog[i] = Frame.get(i);
        }
        int repeat = Integer.valueOf(Token);
        for(int i = 0; i < repeat; i++){
            progIdx = 0;
            progIdx_F = 0;
            progIdx_K = 0;
            mainstart();
        }
        progIdx = progIdx_b;
        progIdx_F = progIdx_fb;
        progIdx_K = progIdx_kb;
        prog = prog_b;
    }

    private void fun_if() throws InterpreterException {
        double result;
        getFrame();
        result = evaluate();
        if (result != 0.0) {
            getFrame();
            Frame.add('\0');
            int progIdx_b = progIdx;
            int progIdx_fb = progIdx_F;
            int progIdx_kb = progIdx_K;
            char[] prog_b = prog;
            for(int i = 0; i < Frame.size(); i++){
                prog[i] = Frame.get(i);
            }
            progIdx = 0;
            progIdx_F = 0;
            progIdx_K = 0;
            mainstart();
            progIdx = progIdx_b;
            progIdx_F = progIdx_fb;
            progIdx_K = progIdx_kb;
            prog = prog_b;
        }
        else{
            getFrame();
            getFrame();
            Frame.add('\0');
            int progIdx_b = progIdx;
            int progIdx_fb = progIdx_F;
            int progIdx_kb = progIdx_K;
            char[] prog_b = prog;
            for(int i = 0; i < Frame.size(); i++){
                prog[i] = Frame.get(i);
            }
            progIdx = 0;
            progIdx_F = 0;
            progIdx_K = 0;
            mainstart();
            progIdx = progIdx_b;
            progIdx_F = progIdx_fb;
            progIdx_K = progIdx_kb;
            prog = prog_b;
        }
    }

    private void fun_return() throws InterpreterException {
        progIdx_K = fun_stack.pop();
        progIdx = fun_stack.pop();
    }

    private void print() throws InterpreterException {
        getFrame();
        getToken();
        switch (TokenType){
            case VARIABLE:
                if(var_label.get(Token) != null){
                    System.out.println(var_label.get(Token));
                }
                else{
                    handleErr(7);
                }
                break;
            case QUOTEDSTR:
                System.out.println(Token);
                progIdx--;
                break;
            case NUMBER:
                System.out.println(Integer.valueOf(Token));
                break;
        }
    }

    private double evaluate() throws InterpreterException{
        double result = 0.0;
        getToken();
        result = relop();
        return result;
    }

    private double relop() throws InterpreterException{
        double l_temp, r_temp, result;
        char op;
        result = addop();
        op = Token.charAt(0);
        if(isRelop(op)) {
            l_temp = result;
            getToken();
            r_temp = relop();
            switch(op) { // perform the relational operation
                case '<':
                    if(l_temp < r_temp) result = 1.0;
                    else result = 0.0;
                    break;
                case LE:
                    if(l_temp <= r_temp) result = 1.0;
                    else result = 0.0;
                    break;
                case '>':
                    if(l_temp > r_temp) result = 1.0;
                    else result = 0.0;
                    break;
                case GE:
                    if(l_temp >= r_temp) result = 1.0;
                    else result = 0.0;
                    break;
                case '=':
                    if(l_temp == r_temp) result = 1.0;
                    else result = 0.0;
                    break;
                case NE:
                    if(l_temp != r_temp) result = 1.0;
                    else result = 0.0;
                    break;
            }
        }
        return result;
    }

    private double addop() throws InterpreterException{
        char op;
        double result;
        double partialResult;
        result = mulop();
        while((op = Token.charAt(0)) == '+' || op == '-') {
            getToken();
            partialResult = mulop();
            switch(op) {
                case '-':
                    result = result - partialResult;
                    break;
                case '+':
                    result = result + partialResult;
                    break;
            }
        }
        return result;
    }

    private double mulop() throws InterpreterException {
        char op;
        double result;
        double partialResult;
        result = atom();
        while((op = Token.charAt(0)) == '*' ||
                op == '/' || op == '%') {
            getToken();
            partialResult = atom();
            switch (op) {
                case '*':
                    result = result * partialResult;
                    break;
                case '/':
                    if (partialResult == 0.0)
                        handleErr(3);
                    result = result / partialResult;
                    break;
                case '%':
                    if (partialResult == 0.0)
                        handleErr(3);
                    result = result % partialResult;
                    break;
            }
        }
        return result;
    }

    private double atom() throws InterpreterException{
        double result = 0.0;
        switch(TokenType){
            case NUMBER:
                result = Double.parseDouble(Token);
                getToken();
                break;
            case VARIABLE:
                result = var_label.get(Token);
                getToken();
                break;
        }
        return result;
    }

    private void handleErr(int error) throws InterpreterException {
        String[] err = {
                "Wrong File!",
                "변수가 아닙니다.",
                "숫자가 아닙니다.",
                "나눗셈을 할 수 없습니다.",
                "입력값 에러입니다.",
                "\"가 없습니다.",
                "구문 에러!",
                "없는 변수입니다."
        };
        throw new InterpreterException(err[error]);
    }

    private void keyword_init(){
        kwToken = "";
        kwTktype = 0;
    }
    private int lookup(String s){
        // 토큰테이블에서 토큰을 검색
        for(int i=0; i < kwtable.length; i++)
            if(kwtable[i].keyword.equals(s))
                return kwtable[i].keywordTok;
        return UNKNCOM; // unknown keyword
    }

    boolean isSpaceOrTab(char c) {
        if(c == ' ' || c =='\t') return true;
        return false;
    }

    boolean isRelop(char c) {
        if(relops.indexOf(c) != -1) return true;
        return false;
    }

    private boolean isDelim(char c)
    {
        if((" \r+-/*=".indexOf(c) != -1))
            return true;
        return false;
    }
}

public class Myinterpreter {
    public static void main(String args[]) {
        try {
            interpreter i = new interpreter("TEST.txt");
            i.run();
        }catch(InterpreterException exc) {
            System.out.println(exc);
        }
    }
}