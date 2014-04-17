package ch.lws.demo.activiti;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
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
 * changes are transmitted through Activiti-forms.<br>
 * 
 * <pre>
 * <li>historic-config "audit" is sufficiant
 * <li>no complete Order-objects avaiable, only single properties in "log-style"
 * </pre>
 * 
 * @author pfeutia
 * 
 */
public class OrderProcessObjectFormTest {

	private static final Logger LOG = LoggerFactory
			.getLogger(OrderProcessObjectFormTest.class);

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

		// TODO start process with form-service -> so we would have the initial
		// state in the history

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

		FormService formService = activitiRule.getFormService();
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
			// update variable with form
			Map<String, String> updates = new HashMap<String, String>();
			updates.put("organisationName", ((Order) currentTask
					.getProcessVariables().get("order")).getAddress()
					.getOrganisationName()
					+ " updated");
			formService.submitTaskFormData(currentTask.getId(), updates);
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
			// update variable as form
			Map<String, String> updates = new HashMap<String, String>();

			updates.put("organisationName", ((Order) currentTask
					.getProcessVariables().get("order")).getAddress()
					.getOrganisationName()
					+ " updated2");

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

		System.out.println("===================================");

		// if historic is set to "full", here we find the initial order, but not
		// the updates, since the updates where done through forms
		// if historic is set to "audit", nothing is found here
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