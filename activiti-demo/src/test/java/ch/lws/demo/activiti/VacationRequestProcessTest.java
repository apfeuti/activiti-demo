package ch.lws.demo.activiti;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VacationRequestProcessTest {

	private static final Logger LOG = LoggerFactory
			.getLogger(VacationRequestProcessTest.class);
	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	@Deployment(resources = { "VacationRequest.bpmn20.xml" })
	public void test() {

		Map<String, Object> variables1 = new HashMap<String, Object>();
		variables1.put("employeeName", "Kermit");
		variables1.put("numberOfDays", new Integer(4));
		variables1.put("vacationMotivation", "I'm really tired!");

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey("vacationRequest", variables1);

		Map<String, Object> variables2 = new HashMap<String, Object>();
		variables2.put("employeeName", "Arthur");
		variables2.put("numberOfDays", new Integer(10));
		variables2.put("vacationMotivation", "Bike and ride");

		ProcessInstance processInstance2 = activitiRule.getRuntimeService()
				.startProcessInstanceByKey("vacationRequest", variables2);
		assertNotNull(processInstance2);

		// Task task = activitiRule.getTaskService().createTaskQuery()
		// .singleResult();
		// assertEquals("Handle vacation request", task.getName());

		// Verify that we started a new process instance
		LOG.info("Number of process instances: "
				+ activitiRule.getRuntimeService().createProcessInstanceQuery()
						.count());

		// Fetch all tasks for the management group
		TaskService taskService = activitiRule.getTaskService();
		List<Task> tasks = taskService.createTaskQuery()
				.taskCandidateGroup("management").includeProcessVariables()
				.list();
		for (Task currentTask : tasks) {
			LOG.info("Task available: " + currentTask.getName() + " vars: "
					+ currentTask.getProcessVariables());
		}

		Task rejectedTask = tasks.get(0);

		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("vacationApproved", "false");
		taskVariables.put("managerMotivation", "We have a tight deadline!");
		taskService.complete(rejectedTask.getId(), taskVariables);

		tasks = taskService.createTaskQuery().taskAssignee("Kermit")
				.includeProcessVariables().list();
		for (Task currentTask : tasks) {
			LOG.info("Task available for Kermit to adjust: "
					+ currentTask.getName() + " vars: "
					+ currentTask.getProcessVariables());
		}
	}

}
