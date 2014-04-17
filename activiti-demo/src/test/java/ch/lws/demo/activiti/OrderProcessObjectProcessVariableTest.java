package ch.lws.demo.activiti;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.lws.demo.activiti.domain.Address;
import ch.lws.demo.activiti.domain.Order;

/**
 * changes are transmitted as variables.<br>
 * 
 * <pre>
 * <li>historic-config must be set to "full"
 * <li>historized complete order-objects are available
 * </pre>
 * 
 * @author pfeutia
 * 
 */
public class OrderProcessObjectProcessVariableTest {

	private static final Logger LOG = LoggerFactory
			.getLogger(OrderProcessObjectProcessVariableTest.class);

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	@Deployment(resources = { "OrderProcessObject.bpmn20.xml" })
	public void startProcess() throws Exception {

		RuntimeService runtimeService = activitiRule.getRuntimeService();

		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("conditionDeterminer", new ConditionDeterminer());
		Order order1 = new Order(1, new Address("UBS AG", "F-88998 Paris",
				"1200"));
		vars.put("order", order1);

		ProcessInstance processInstanceUBS = runtimeService
				.startProcessInstanceByKey("orderProcess", vars);
		assertNotNull(processInstanceUBS.getId());
		System.out.println("id " + processInstanceUBS.getId() + " "
				+ processInstanceUBS.getProcessDefinitionId());
		String procIdUBS = processInstanceUBS.getId();

		// vars = new HashMap<String, Object>();
		// vars.put("conditionDeterminer", new ConditionDeterminer());
		// vars.put("order", new Order(2, new Address("Coop", "2500", null)));
		//
		// ProcessInstance processInstance = runtimeService
		// .startProcessInstanceByKey("orderProcess", vars);
		// assertNotNull(processInstance.getId());
		// System.out.println("id " + processInstance.getId() + " "
		// + processInstance.getProcessDefinitionId());

		// Fetch all tasks for the Kundenberater
		TaskService taskService = activitiRule.getTaskService();
		List<Task> tasks = taskService.createTaskQuery()
				.taskCandidateGroup("Z-PP-SSO-PBP-Kundenberater")
				.includeProcessVariables().list();
		for (Task currentTask : tasks) {
			LOG.info("Task available for Z-PP-SSO-PBP-Kundenberater: "
					+ currentTask.getName() + " vars: "
					+ currentTask.getProcessVariables().get("order"));
			taskService.claim(currentTask.getId(), "Andreas");

			// update variable as process-variable
			order1.getAddress().setOrganisationName(
					order1.getAddress().getOrganisationName() + " updated");
			// runtimeService.setVariable(procIdUBS, "order", order1);
			Map<String, Object> updates = new HashMap<String, Object>();
			updates.put("order", order1);
			taskService.complete(currentTask.getId(), updates);
		}

		// Fetch all tasks for the SCF West
		tasks = taskService.createTaskQuery()
				.taskCandidateGroup("Z-PP-SSO-PBP-FS-SCF-WEST")
				.includeProcessVariables().list();
		for (Task currentTask : tasks) {
			LOG.info("Task available for Z-PP-SSO-PBP-FS-SCF-WEST: "
					+ currentTask.getName() + " vars: "
					+ currentTask.getProcessVariables().get("order"));

			taskService.claim(currentTask.getId(), "Laurin");

			// update variable as process-variable
			order1.getAddress().setOrganisationName(
					order1.getAddress().getOrganisationName() + " updated2");
			// runtimeService.setVariable(procIdUBS, "order", order1);
			Map<String, Object> updates = new HashMap<String, Object>();
			updates.put("order", order1);
			taskService.complete(currentTask.getId(), updates);

		}

		HistoryService historyService = activitiRule.getHistoryService();
		// get all changes on form-properties on a given process -> find
		// nothing, since updates are NOT done through forms.
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

		System.out.println("===================================");

		List<HistoricDetail> details = historyService
				.createHistoricDetailQuery().variableUpdates()
				.processInstanceId(procIdUBS).orderByVariableName().asc()
				.orderByVariableRevision().asc().list();
		for (HistoricDetail current : details) {
			HistoricDetailVariableInstanceUpdateEntity detail = (HistoricDetailVariableInstanceUpdateEntity) current;

			// lookup user who did the change
			HistoricActivityInstance hai = historyService
					.createHistoricActivityInstanceQuery()
					.activityInstanceId(detail.getActivityInstanceId())
					.singleResult();
			;

			if (detail.getName().equals("order")) {
				Order order = (Order) detail.getValue();
				System.out.println("User: " + hai.getAssignee() + ", order "
						+ order + ", ByteArrayId: "
						+ detail.getByteArrayValue().getId());
			}
		}

		TestHelper.startDBBrowser();

	}
}