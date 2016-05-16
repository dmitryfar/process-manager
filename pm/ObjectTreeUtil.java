package org.activiti.pm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTreeUtil {
  private static Logger logger = LoggerFactory.getLogger(ObjectTreeUtil.class);

  public static JSONObject getObjectTree(Object obj) {
    return getObjectTree(obj, 0, false);
  }

  public static JSONObject getFullObjectTree(Object obj) {
    return getObjectTree(obj, 0, true);
  }

  private static JSONObject getObjectTree(Object obj, int level, boolean addNullValues) {
    if (level > 25) {
      throw new IllegalArgumentException("level = " + level + ", obj=" + obj);
    }
    JSONObject jsonObject = new JSONObject();


    if (obj != null) {
      if (level == 0) {
        jsonObject.put("text", obj.getClass().getName());
      }
      jsonObject.put("level", level);

      Class<?> clazz = obj.getClass();

      Field[] fields = getAllFields(clazz );
      JSONArray nodes = new JSONArray();
      for (Field field : fields) {
        field.setAccessible(true);
        Object fieldValue = null;
        try {
          fieldValue = field.get(obj);
          if (fieldValue == null && !addNullValues) {
            continue;
          }
          Class<?> fieldClass = field.getType();
          if (fieldValue != null) {
            fieldClass = fieldValue.getClass();
          }
          JSONObject node = null;
          if (fieldValue instanceof List) {
            node = getNodeWithChilds(fieldClass, field.getName(), fieldValue, level, addNullValues);
          } else {
            node = getNode(fieldClass, field.getName(), fieldValue, level, addNullValues);
          }
          nodes.put(node);
        } catch (IllegalArgumentException e) {
          logger.error(e.getMessage());
        } catch (IllegalAccessException e) {
          logger.error(e.getMessage());
        }
      }
      jsonObject.put("nodes", nodes);
    }
    return jsonObject;
  }

  private static Field[] getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<Field>();
    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
    if (clazz.getSuperclass() != null) {
        fields.addAll(Arrays.asList(getAllFields(clazz.getSuperclass())));
    }
    return fields.toArray(new Field[] {});
  }

  private static JSONObject getNodeWithChilds(Class<?> fieldType, String fieldName, Object fieldValue, int level, boolean addNullValues) {
    JSONObject node = new JSONObject();

    String name = "<span class='field-name'>" + fieldName + "</span>";
    String className = "<span class='field-class'>" + fieldType.getName() + "</span>";

    String text = name + " (" + className + ")";

    node.put("text", text);
    node.put("isList", true);
    JSONArray nodeList = new JSONArray();
    int i = 0;
    for (Object valueItem : (List<?>)fieldValue) {
      JSONObject item = null;
      if (valueItem != null) {
        // String itemName = "<span class='field-name'>" + fieldName + "</span>";
        String itemClassName = "<span class='field-class'>" + valueItem.getClass().getName() + "</span>";

        String itemText = name + "[" + i + "] (" + itemClassName + ")";
        item = getObjectTree(valueItem, level+1, addNullValues);
        item.put("text", itemText);
        nodeList.put(item);
      } else if (addNullValues){
        nodeList.put(item);
      }
      i++;
    }
    node.put("nodes", nodeList);
    return node;
  }


  private static JSONObject getNode(Class<?> fieldType, String fieldName, Object fieldValue, int level, boolean addNullValues) {
    JSONObject node = new JSONObject();
    String text = null;

    String name = "<span class='field-name'>" + fieldName + "</span>";
    String className = "<span class='field-class'>" + fieldType.getName() + "</span>";
    String value = "<span class='field-value'>" + fieldValue + "</span>";

    if (fieldValue != null) {
      if (fieldValue instanceof Enum) {
        text = name + " (" + className + ") = " + value;
      } else if (fieldValue.getClass().getName().startsWith("ru.yota")) {
        text = name + " (" + className + ")";
        JSONObject innerNode = getObjectTree(fieldValue, level+1, addNullValues);
        node.put("nodes", innerNode.get("nodes"));
      } else {
        text = name + " (" + className + ") = " + value;
      }
    } else {
      text = name + " (" + className + ") = null";
      node.put("isNull", true);
    }
    node.put("text", text);
    return node;
  }
}
