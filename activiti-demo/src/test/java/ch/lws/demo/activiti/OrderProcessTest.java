package ch.lws.demo.activiti;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderProcessTest {

	private static final Logger LOG = LoggerFactory
			.getLogger(OrderProcessTest.class);

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	@Deployment(resources = { "OrderProcess.bpmn20.xml" })
	public void startProcess() throws Exception {

		RuntimeService runtimeService = activitiRule.getRuntimeService();

		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("conditionDeterminer", new ConditionDeterminer());
		vars.put("organisationName", "UBS AG");
		vars.put("zip", "F-88998 Paris");
		vars.put("loadingPointZip", "1200");

		ProcessInstance processInstanceUBS = runtimeService
				.startProcessInstanceByKey("orderProcess", vars);
		assertNotNull(processInstanceUBS.getId());
		System.out.println("id " + processInstanceUBS.getId() + " "
				+ processInstanceUBS.getProcessDefinitionId());
		String procIdUBS = processInstanceUBS.getId();

		vars = new HashMap<String, Object>();
		vars.put("conditionDeterminer", new ConditionDeterminer());
		vars.put("organisationName", "Coop");
		vars.put("zip", "2500");
		vars.put("loadingPointZip", null);

		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByKey("orderProcess", vars);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());

		FormService formService = activitiRule.getFormService();
		// Fetch all tasks for the Kundenberater
		TaskService taskService = activitiRule.getTaskService();
		List<Task> tasks = taskService.createTaskQuery()
				.taskCandidateGroup("Z-PP-SSO-PBP-Kundenberater")
				.includeProcessVariables().list();
		for (Task currentTask : tasks) {
			LOG.info("Task available for Z-PP-SSO-PBP-Kundenberater: "
					+ currentTask.getName() + " vars: "
					+ currentTask.getProcessVariables().get("organisationName"));
			taskService.claim(currentTask.getId(), "Andreas");
			// taskService.complete(currentTask.getId());
			// update variable
			Map<String, String> updates = new HashMap<String, String>();
			updates.put("organisationName", currentTask.getProcessVariables()
					.get("organisationName") + " updated");
			formService.submitTaskFormData(currentTask.getId(), updates);
		}

		// Fetch all tasks for the SCF West
		tasks = taskService.createTaskQuery()
				.taskCandidateGroup("Z-PP-SSO-PBP-FS-SCF-WEST")
				.includeProcessVariables().list();
		for (Task currentTask : tasks) {
			LOG.info("Task available for Z-PP-SSO-PBP-FS-SCF-WEST: "
					+ currentTask.getName() + " vars: "
					+ currentTask.getProcessVariables().get("organisationName"));

			taskService.claim(currentTask.getId(), "Laurin");
			// taskService.complete(currentTask.getId());
			// update variable
			Map<String, String> updates = new HashMap<String, String>();
			updates.put("organisationName", currentTask.getProcessVariables()
					.get("organisationName") + " updated");
			formService.submitTaskFormData(currentTask.getId(), updates);

		}

		HistoryService historyService = activitiRule.getHistoryService();
		// get all changes on form-properties on a given process
		List<HistoricDetail> result = historyService
				.createHistoricDetailQuery().formProperties()
				.processInstanceId(procIdUBS).orderByVariableName().asc()
				.orderByVariableRevision().asc().list();

		for (HistoricDetail current : result) {
			// lookup, WHO did the change
			HistoricTaskInstance hti = historyService
					.createHistoricTaskInstanceQuery()
					.taskId(current.getTaskId()).singleResult();

			// System.out.println(hti);
			HistoricFormProperty hfp = (HistoricFormProperty) current;
			System.out.println(hfp.getPropertyId() + ": "
					+ hfp.getPropertyValue() + ", " + hti.getAssignee() + ", "
					+ hfp.getTime());
		}

	}
}