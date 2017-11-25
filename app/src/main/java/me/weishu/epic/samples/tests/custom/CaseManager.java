package me.weishu.epic.samples.tests.custom;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by weishu on 17/11/6.
 */

public class CaseManager {

    private static volatile CaseManager INSTANCE = new CaseManager();

    private Map<Class<?>, Case> caseMap = new ConcurrentHashMap<>();

    public static synchronized CaseManager getInstance() {
        return INSTANCE;
    }

    public synchronized Case getCase(Class<?> clazz) {
        Case caze = caseMap.get(clazz);
        if (caze != null) {
            return caze;
        } else {
            try {
                caze = (Case) clazz.newInstance();
                caseMap.put(clazz, caze);
            } catch (Throwable e) {
                throw new RuntimeException("can not get case !!", e);
            }
        }
        return caze;
    }

    public synchronized Set<Class<?>> getCases() {
        return caseMap.keySet();
    }
}
