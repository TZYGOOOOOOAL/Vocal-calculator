package edu.whut.taozeyuan.calculator;

import java.text.NumberFormat;
import java.util.LinkedList;


public class MyCalculator {

    private static int MAX_FRACTION_DIGITS = 2;  //结果保留最大位数
    private String inputStr;
    private double ans;

    public MyCalculator(String inputStr,double ans){
        this.inputStr=inputStr;
        this.ans=ans;
    }

    public double calculate() throws Exception{
        double num2;
        double num1;
        double result;
        boolean lastIsNum=false;	//上一次为数字，用于高于1位数的运算
        boolean lastHadPoint=false; //之前有小数点存在
        int    pointDigit=1;		//小数点位数
        MyStack<Double> stackNum=new MyStack<>();
        MyStack<Character> stackChar=new MyStack<>();
        //字符串处理,用ans值替换字符串中ANS
        inputStr=inputStr.replace("ANS",""+ans);
        StringBuilder stringBuilder=new StringBuilder(inputStr);
        for(int i=0;i<stringBuilder.length()-1;i++){
            if(stringBuilder.charAt(i)=='(' &&
                    priority(stringBuilder.charAt(i+1))<=1 &&
                    priority(stringBuilder.charAt(i+1))>=0)     //如果'('后第一字符是数字或+-则填前导0
                stringBuilder.insert(i+1,'0');
        }
        inputStr=stringBuilder.toString();
        if(priority(inputStr.charAt(0))<=1 &&
                priority(inputStr.charAt(0))>=0)     //输入字符串首字符为数字或+-号设置前导0，计算负数
            inputStr="0"+inputStr;

        for(int i=0;i<inputStr.length();i++){
            char chr=inputStr.charAt(i);

            if(chr>='0'&&chr<='9'||chr=='.')
            {
                if(chr=='.'){   //有小数点情况
                    lastHadPoint=true;
                }else{
                    double num=chr+0-'0';
                    if(lastIsNum){      //上一个为数字；分为有小数点情况和无小数点情况
                        if(lastHadPoint){
                            pointDigit=10*pointDigit;
                            num=(stackNum.pop()*pointDigit+num)/pointDigit;
                        }else{
                            num=stackNum.pop()*10+num;
                        }
                    }
                    stackNum.push(num);		//数字入栈
                }
                lastIsNum=true;
                continue;
            }
            else        //运算符情况
            {
                lastIsNum=false;
                lastHadPoint=false;
                pointDigit=1;

                if(chr!='('&&chr!=')')
                {
                    //字符需要处理
                    //输入运算符优先级>栈顶符号优先级，则符号入栈，不执行运算
                    if(stackChar.getLen()>0){		//符号栈不为空
                        if(priority(chr)>priority(stackChar.getTop())){
                            stackChar.push(chr);
                        }else{
                            num2=stackNum.pop();
                            num1=stackNum.pop();
                            double result2=cal2(num1,num2,stackChar.pop());
                            stackNum.push(result2);
                            stackChar.push(chr);
                        }
                    }else{
                        stackChar.push(chr);
                    }
                }

                if(chr=='('){
                    stackChar.push(chr);
                }

                if(chr==')')
                {
                    while(stackChar.getTop()!='(')
                    {
                        num2=stackNum.pop();
                        num1=stackNum.pop();
                        double result3=cal2(num1,num2,stackChar.pop());
                        stackNum.push(result3);
                    }
                    stackChar.pop();
                }

            }
        }
        //最后栈中优先级由小到大的
        //可直接从尾到手运算
        while(stackNum.getLen()>=2){
            num2=stackNum.pop();
            num1=stackNum.pop();
            result=cal2(num1,num2,stackChar.pop());
            stackNum.push(result);
        }
        if(!stackChar.isEmpty())    //运算符栈不为空，抛出异常
            throw new Exception("输入有误！");
        double ret=stackNum.pop();
        NumberFormat numberFormat=NumberFormat.getNumberInstance();
//        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);       //保留两位
        numberFormat.setGroupingUsed(false);                //数字转换不用','间隔
        ret=Double.parseDouble(numberFormat.format(ret));
        return ret;
    }
    //优先级
    private int priority(char inChar){
        int ret;
        switch(inChar){
            case '+':
            case '-': ret=1;  break;
            case '×':
            case '÷': ret=2;  break;
            case '(': ret=-1; break;
            default : ret=0;
        }
        return ret;
    }

    private double cal2(double num1,double num2,char operator) throws Exception {
        switch(operator){
            case '+': return num1+num2;
            case '-': return num1-num2;
            case '×': return num1*num2;
            case '÷':
                if(num2==0)
                    throw new Exception("除数为0！");
                else
                    return num1/num2;
            default : return 0;
        }
    }

}


class MyStack<T>{
    private LinkedList<T> stackList=new LinkedList<>();

    public void push(T data){
        stackList.add(data);
    }

    public T pop() throws Exception {
        if(stackList.isEmpty())
            throw new Exception("输入有误！");
        else {
            T ret;
            ret=stackList.getLast();
            stackList.removeLast();
            return ret;
        }
    }

    public T getTop() throws Exception {
        if(stackList.isEmpty())
            throw new Exception("输入有误！");
        else
            return stackList.getLast();
    }

    public int getLen(){
        return stackList.size();
    }

    public boolean isEmpty(){
        return stackList.isEmpty();
    }
}
