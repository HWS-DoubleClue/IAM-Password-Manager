package com.doubleclue.dcem.core.gui;

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;


/**
 * Note: Internal use only.
 *
 * Message bundle handler class which holds MessageBundle for a given property file name.
 *
 */
public class MessageBundleHandler {
   private static MessageBundleHandler sSingleton = new MessageBundleHandler();

   // key - bundle file name
   // value - MessageBundle object
   private static Map<String, MessageBundle> sBundleHandler = new HashMap<String, MessageBundle>();

   private MessageBundleHandler() {
   }

   /**
    * Returns singleton object
    * @return
    */
   public static MessageBundleHandler getInstance() {
      return sSingleton;
   }

   /**
    * Returns MessageBundle object
    * @param bundleName
    * @return
    */
   private MessageBundle getMessageBundle(String bundleName) {
      if (bundleName == null) {
         throw new NullPointerException("Bundle Name cannot be null");
      }

      if (bundleName.length() == 0) {
         throw new IllegalStateException("Bundle Name cannot be empty");
      }
      if (sBundleHandler.get(bundleName) == null) {
         synchronized (bundleName) {
            sBundleHandler.put(bundleName, new MessageBundle(bundleName));
         }
      }

      return sBundleHandler.get(bundleName);
   }

   /**
    * Returns message for a given message id and parameters value. Parameter holders
    * {0},{1},etc will be replaced by respective parametersValue.
    *
    * @param pSeverity
    * @param pBundleName
    * @param pMessageId
    * @param pParametersValue
    * @param pUIComponent
    * @return
    */
   public FacesMessage getMessage(FacesMessage.Severity pSeverity, String pBundleName, String pMessageId, Object[] pParametersValue,
                                  UIComponent pUIComponent) {
      MessageBundle messageBundle = getMessageBundle(pBundleName);
      String summary = messageBundle.getMessage(pMessageId, pParametersValue);
      String detail = null;
      if (messageBundle.containsMessage(pMessageId + "_detail")) {
         detail = messageBundle.getMessage(pMessageId + "_detail", pParametersValue);
      } else {
         detail = summary;
      }
//      if (pUIComponent == null) {
         return new FacesMessage(pSeverity, summary, detail);
//      } else {
//         return new FacesMessage(pSeverity, summary, detail, JSFUtils.getLabel(pUIComponent));
//      }
   }
}
