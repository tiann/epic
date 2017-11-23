package me.weishu.epic.samples.tests;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weishu on 17/11/13.
 */

public class TestSuite {
    private String name;
    private List<TestCase> cases = new ArrayList<>();

    public TestSuite(String name) {
        this.name = name;
    }

    public void addCase(TestCase caze) {
        cases.add(caze);
    }

    public String getName() {
        return name;
    }

    public List<TestCase> getAllCases() {
        return cases;
    }
}
