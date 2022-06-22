package com.doubleclue.dcem.core.gui;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;


import org.primefaces.util.MessageFactory;

@FacesComponent(Password.COMPONENT_TYPE)
public class Password extends org.primefaces.component.password.Password {
    public static final String INVALID_STRENGTH_LEVEL = "XXX.password.INVALID_STRENGTH_LEVEL";

    public static final String COMPONENT_FAMILY = "XXX.component";
    public static final String COMPONENT_TYPE = "XXX.password.Password";
    public static final String RENDERER_TYPE = "XXX.password.PasswordRenderer";
    
    protected enum PropertyKeys {
        minScore;
        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }
        PropertyKeys() {}
        public String toString() {
            return ((this.toString != null) ? this.toString : super.toString());
        }
    }
    
    private Pattern patternNumber;
    private Pattern patternAlfa;
    private Pattern patternSpecial;
    private Pattern patternUpper;
    

    public Password() {
        patternNumber = Pattern.compile("[0-9]");
        patternAlfa = Pattern.compile("[a-zA-Z]");
        patternSpecial = Pattern.compile("[!@#$%^&*?_~.,;=]");
        patternUpper = Pattern.compile("[A-Z]");
    }
    
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
    
    @Override
    public String getRendererType() {
        return RENDERER_TYPE;
    }
    
    public int getMinScore() {
        return (Integer) getStateHelper().eval(PropertyKeys.minScore, 0);
    }
    
    public void setMinScore(int _minScore) {
        getStateHelper().put(PropertyKeys.minScore, _minScore);
    }
    
    @Override
    protected void validateValue(FacesContext context, Object value) {
        super.validateValue(context, value);
        Object submittedValue = this.getSubmittedValue();
        int minScore = getMinScore();
        
        if (isValid() && minScore > 0) {
            if (submittedValue != null) {
                int score = testStrength(submittedValue.toString());

                if (score < minScore) {
                    this.setValid(false);
                    Object[] params = new Object[1];
                    params[0] = MessageFactory.getLabel(context, this);
   //                 context.addMessage(getClientId(context), MessageFactory.getMessage(Password.INVALID_STRENGTH_LEVEL, FacesMessage.SEVERITY_ERROR, params));
                }
            }
        }
    }
    
    private int count(Pattern pattern, String str) {
        Matcher matcher = pattern.matcher(str);

        int val = 0;
        while (matcher.find()) {
            val ++;
        }
        return val;
    }

    private int testStrength(String str) {
        int grade = 0;

        int val = count(patternNumber, str);
        grade += normalize(val > 0 ? val : 1f/4, 1) * 25;

        val = count(patternAlfa, str);
        grade += normalize(val > 0 ? val : 1f/2, 3) * 10;

        val = count(patternSpecial, str);
        grade += normalize(val > 0 ? val : 1f/6, 1) * 35;

        val = count(patternUpper, str);
        grade += normalize(val > 0 ? val : 1f/6, 1) * 30;

        grade *= str.length() / 8f;

        return grade > 100 ? 100 : grade;
    }

    private double normalize(double x, double y) {
        double diff = x - y;

        if(diff <= 0) {
            return x / y;
        } else {
            return (1 + 0.5 * (x / (x + y/4)));
        }
    }
}


