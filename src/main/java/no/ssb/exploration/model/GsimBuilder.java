package no.ssb.exploration.model;

import no.ssb.dapla.dataset.doc.model.simple.EnumInfo;
import no.ssb.dapla.dataset.doc.model.simple.TypeInfo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GsimBuilder {

    private GsimBuilder() {
    }

    public static BaseBuilder create() {
        return new BaseBuilder();
    }

    public static class BaseBuilder {
        private String id;
        private String name;
        private String createdBy;
        private String description;

        private String languageCode = "nb";

        private Map<String, Object> properties = new HashMap<>();

        public BaseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public String getLanguageCode() {
            return languageCode;
        }

        public BaseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BaseBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public BaseBuilder languageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public BaseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BaseBuilder addProperty(String key, Object value) {
            properties.put(key, value);
            return this;
        }

        public BaseBuilder build(IdentifiableArtefact identifiableArtefact) {
            if (name != null) {
                identifiableArtefact.setUnknowProperty("name", createListOfMap(languageCode, name));
            }
            if (description != null && !description.isEmpty()) {
                identifiableArtefact.setUnknowProperty("description", createListOfMap(languageCode, description));
            }
            if (id == null) {
                throw new IllegalArgumentException("id is missing");
            }
            identifiableArtefact.setId(id);

            if (createdBy != null) {
                properties.put("createdBy", createdBy);
            }
            properties.forEach(identifiableArtefact::setUnknowProperty);
            return this;
        }

        public UnitDatasetBuilder unitDataSet() {
            return new UnitDatasetBuilder(this);
        }

        public UnitDataStructureBuilder unitDataStructure() {
            return new UnitDataStructureBuilder(this);
        }

        public LogicalRecordBuilder logicalRecord() {
            return new LogicalRecordBuilder(this);
        }

        public InstanceVariableBuilder instanceVariable() {
            return new InstanceVariableBuilder(this);
        }

        public BusinessProcessBuilder businessProcess() {
            return new BusinessProcessBuilder(this);
        }

        public StatisticalProgramCycleBuilder statisticalProgramCycle() {
            return new StatisticalProgramCycleBuilder(this);
        }

        public ProcessStepBuilder processStep() {
            return new ProcessStepBuilder(this);
        }

        public ProcessStepInstanceBuilder processStepInstance() {
            return new ProcessStepInstanceBuilder(this);
        }

        public TransformableInputBuilder transformableInput() {
            return new TransformableInputBuilder(this);
        }

        public TransformedOutputBuilder transformableOutput() {
            return new TransformedOutputBuilder(this);
        }

        public ProcessExecutionLogBuilder processExecutionLog() {
            return new ProcessExecutionLogBuilder(this);
        }

        public StatisticalProgramBuilder statisticalProgram() {
            return new StatisticalProgramBuilder(this);
        }

        public RepresentedVariableBuilder representedVariable() {
            return new RepresentedVariableBuilder(this);
        }

        public DescribedValueDomainBuilder describedValueDomain() {
            return new DescribedValueDomainBuilder(this);
        }
    }

    public static class UnitDatasetBuilder {
        private final BaseBuilder baseBuilder;
        private UnitDataSet unitDataset = new UnitDataSet();


        public UnitDatasetBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }


        public UnitDatasetBuilder dataSourcePath(String dataSourcePath) {
            unitDataset.setDataSourcePath(dataSourcePath);
            return this;
        }

        public UnitDatasetBuilder temporalityType(String temporalityType) {
            baseBuilder.addProperty("temporalityType", temporalityType);
            return this;
        }

        public UnitDatasetBuilder dataSetState(String dataSetState) {
            baseBuilder.addProperty("dataSetState", dataSetState);
            return this;
        }

        public UnitDatasetBuilder lineage(String lineageDataset) {
            unitDataset.setLineage(lineageDataset);
            return this;
        }

        public UnitDataSet build() {
            baseBuilder.build(unitDataset);
            return unitDataset;
        }

        public UnitDatasetBuilder unitDataStructure(String unitDataStructureGuid) {
            unitDataset.setUnitDataStructure("/UnitDataStructure/" + unitDataStructureGuid);
            return this;
        }
    }

    public static class UnitDataStructureBuilder {
        private final BaseBuilder baseBuilder;
        private UnitDataStructure unitDataStructure = new UnitDataStructure();

        public UnitDataStructureBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public UnitDataStructureBuilder logicalRecords(List<String> logicalRecords) {
            List<String> logicalRecordsWithPath = logicalRecords.stream().map(lr -> "/LogicalRecord/" + lr).collect(Collectors.toList());
            unitDataStructure.setLogicalRecords(logicalRecordsWithPath);
            return this;
        }

        public UnitDataStructureBuilder logicalRecord(String id) {
            List<String> logicalRecordsWithPath = Arrays.asList("/LogicalRecord/" + id);
            unitDataStructure.setLogicalRecords(logicalRecordsWithPath);
            return this;
        }

        public UnitDataStructureBuilder noLogicalRecords() {
            unitDataStructure.setLogicalRecords(new ArrayList<>());
            return this;
        }

        public UnitDataStructure build() {
            baseBuilder.build(unitDataStructure);
            return unitDataStructure;
        }
    }

    public static class LogicalRecordBuilder {
        private final BaseBuilder baseBuilder;
        private LogicalRecord logicalRecord = new LogicalRecord();

        public LogicalRecordBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public LogicalRecordBuilder shortName(String shortName) {
            logicalRecord.setShortName(shortName);
            return this;
        }

        public LogicalRecordBuilder instanceVariables(List<String> instanceVariables) {
            List<String> instanceVariablesWithPath = instanceVariables.stream().map(lr -> "/InstanceVariable" + lr).collect(Collectors.toList());
            logicalRecord.setInstanceVariables(instanceVariablesWithPath);
            return this;
        }

        public LogicalRecordBuilder unitType(TypeInfo unitTypeId, String defaultValue) {
            String value = unitTypeId != null ? unitTypeId.getId() : defaultValue;
            logicalRecord.setUnitType("/UnitType/" + value);
            return this;
        }

        public LogicalRecordBuilder isPlaceholderRecord(boolean value) {
            baseBuilder.addProperty("isPlaceholderRecord", value);
            return this;
        }


        public ParentRelationShipBuilder parent(String parentId) {
            ParentRelationShipBuilder parentRelationShipBuilder = new ParentRelationShipBuilder(this);
            if (parentId == null) {
                return parentRelationShipBuilder;
            }
            return parentRelationShipBuilder.parentLogicalRecord(parentId);
        }

        public LogicalRecord build() {
            baseBuilder.build(logicalRecord);
            return logicalRecord;
        }

        public static class ParentRelationShipBuilder {
            private final LogicalRecordBuilder logicalRecordBuilder;

            private ParentRelationShipBuilder(LogicalRecordBuilder logicalRecordBuilder) {
                this.logicalRecordBuilder = logicalRecordBuilder;
            }

            private ParentRelationShipBuilder parentLogicalRecord(String parentId) {
                addProperty("parentLogicalRecord", "/LogicalRecord/" + parentId);
                return this;
            }

            public ParentRelationShipBuilder parentChildMultiplicity(String parentChildMultiplicity) {
                addProperty("parentChildMultiplicity", parentChildMultiplicity);
                return this;
            }

            public LogicalRecord build() {
                return logicalRecordBuilder.build();
            }

            private void addProperty(String key, Object value) {
                logicalRecordBuilder.baseBuilder.addProperty(key, value);
            }
        }
    }

    public static class InstanceVariableBuilder {
        private final BaseBuilder baseBuilder;
        private final InstanceVariable instanceVariable = new InstanceVariable();

        public InstanceVariableBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public InstanceVariableBuilder dataStructureComponentType(EnumInfo dataStructureComponentType, String defaultValue) {
            String value = dataStructureComponentType != null ? dataStructureComponentType.getValue() : defaultValue;
            instanceVariable.setDataStructureComponentType(value);
            return this;
        }

        public InstanceVariableBuilder identifierComponentIsComposite(boolean identifierComponentIsComposite) {
            instanceVariable.setIdentifierComponentIsComposite(identifierComponentIsComposite);
            return this;
        }

        public InstanceVariableBuilder sentinelValueDomain(TypeInfo sentinelValueDomain, String defaultValue) {
            String value = sentinelValueDomain != null ? sentinelValueDomain.getId() : defaultValue;
            instanceVariable.setSentinelValueDomain("/DescribedValueDomain/" + value);
            return this;
        }

        public InstanceVariableBuilder identifierComponentIsUnique(boolean identifierComponentIsUnique) {
            instanceVariable.setIdentifierComponentIsUnique(identifierComponentIsUnique);
            return this;
        }

        public InstanceVariableBuilder dataStructureComponentRole(EnumInfo dataStructureComponentRole, String defaultValue) {
            String value = dataStructureComponentRole != null ? dataStructureComponentRole.getValue() : defaultValue;
            instanceVariable.setDataStructureComponentRole(value);
            return this;
        }

        public InstanceVariableBuilder representedVariable(TypeInfo representedVariable, String defaultValue) {
            String value = representedVariable != null ? representedVariable.getId() : defaultValue;
            instanceVariable.setRepresentedVariable("/RepresentedVariable/" + value);
            return this;
        }

        public InstanceVariableBuilder shortName(String shortName) {
            instanceVariable.setShortName(shortName);
            return this;
        }

        public InstanceVariableBuilder population(TypeInfo population, String defaultValue) {
            String value = population != null ? population.getId() : defaultValue;
            baseBuilder.addProperty("population", "/Population/" + value);
            return this;
        }

        public InstanceVariable build() {
            baseBuilder.build(instanceVariable);
            return instanceVariable;
        }
    }


    public static class RepresentedVariableBuilder {
        private final BaseBuilder baseBuilder;
        private RepresentedVariable representedVariable = new RepresentedVariable();

        public RepresentedVariableBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public RepresentedVariableBuilder substantiveValueDomain(String substantiveValueDomain) {
            representedVariable.setSubstantiveValueDomain(substantiveValueDomain);
            return this;
        }

        public RepresentedVariableBuilder universe(String universe) {
            baseBuilder.addProperty("universe", "/Universe/" + universe);
            return this;
        }

        public RepresentedVariableBuilder variable(String variable) {
            baseBuilder.addProperty("variable", "/Variable/" + variable);
            return this;
        }

        public RepresentedVariable build() {
            baseBuilder.build(representedVariable);
            return representedVariable;
        }
    }

    public static class DescribedValueDomainBuilder {
        private final BaseBuilder baseBuilder;
        private DescribedValueDomain describedValueDomain = new DescribedValueDomain();

        public DescribedValueDomainBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public DescribedValueDomainBuilder dataType(String dataType) {
            describedValueDomain.setDataType(dataType);
            return this;
        }

        public DescribedValueDomainBuilder measurementUnit(String measurementUnit) {
            baseBuilder.addProperty("measurementUnit", measurementUnit);
            return this;
        }

        public DescribedValueDomain build() {
            baseBuilder.build(describedValueDomain);
            return describedValueDomain;
        }
    }

    public static class StatisticalProgramBuilder {
        private final BaseBuilder baseBuilder;
        private StatisticalProgram statisticalProgram = new StatisticalProgram();

        public StatisticalProgramBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public StatisticalProgramBuilder statisticalProgramStatus(String statisticalProgramStatus) {
            statisticalProgram.setStatisticalProgramStatus(statisticalProgramStatus);
            return this;
        }

        public StatisticalProgramBuilder history(String text) {
            if (text != null) {
                baseBuilder.addProperty("history", createListOfMap(baseBuilder.getLanguageCode(), text));
            }
            return this;
        }

        public StatisticalProgramBuilder subjectMatterDomains(String text) {
            if (text != null) {
                statisticalProgram.setSubjectMatterDomains(createListOfMap(baseBuilder.getLanguageCode(), text));
            }
            return this;
        }

        public StatisticalProgramBuilder statisticalProgramCycles(List<String> ids) {
            List<String> statisticalProgramCycles = ids.stream().map(id -> "/StatisticalProgramCycle/" + id).collect(Collectors.toList());
            statisticalProgram.setStatisticalProgramCycles(statisticalProgramCycles);
            return this;
        }

        public StatisticalProgram build() {
            baseBuilder.build(statisticalProgram);
            return statisticalProgram;
        }
    }


    public static class StatisticalProgramCycleBuilder {
        private final BaseBuilder baseBuilder;
        private StatisticalProgramCycle statisticalProgramCycle = new StatisticalProgramCycle();

        public StatisticalProgramCycleBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public StatisticalProgramCycleBuilder businessProcesses(List<String> ids) {
            List<String> processSteps = ids.stream().map(id -> "/BusinessProcess/" + id).collect(Collectors.toList());
            statisticalProgramCycle.setBusinessProcesses(processSteps);
            return this;
        }

        public StatisticalProgramCycleBuilder referencePeriodEnd(String referencePeriodEnd) {
            statisticalProgramCycle.setReferencePeriodEnd(referencePeriodEnd);
            return this;
        }

        public StatisticalProgramCycleBuilder referencePeriodStart(String referencePeriodStart) {
            statisticalProgramCycle.setReferencePeriodStart(referencePeriodStart);
            return this;
        }

        public StatisticalProgramCycle build() {
            baseBuilder.build(statisticalProgramCycle);
            return statisticalProgramCycle;
        }
    }

    public static class BusinessProcessBuilder {
        private final BaseBuilder baseBuilder;
        private BusinessProcess businessProcess = new BusinessProcess();

        public BusinessProcessBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public BusinessProcess build() {
            baseBuilder.build(businessProcess);
            return businessProcess;
        }

        public BusinessProcessBuilder previousBusinessProcess(String id) {
            if (id != null) {
                baseBuilder.addProperty("previousBusinessProcess", "/BusinessProcess/" + id);
            }
            return this;
        }

        public BusinessProcessBuilder parentBusinessProcess(String id) {
            if (id != null) {
                baseBuilder.addProperty("parentBusinessProcess", "/BusinessProcess/" + id);
            }
            return this;
        }

        public BusinessProcessBuilder isPlaceholderProcess(boolean value) {
            businessProcess.setPlaceholderProcess(value);
            return this;
        }

        public BusinessProcessBuilder processSteps(List<String> ids) {
            List<String> processSteps = ids.stream().map(id -> "/ProcessStep/" + id).collect(Collectors.toList());
            businessProcess.setProcessSteps(processSteps);
            return this;
        }

        public BusinessProcessBuilder noProcessSteps() {
            businessProcess.setProcessSteps(new ArrayList<>());
            return this;
        }
    }

    public static class ProcessStepBuilder {
        private final BaseBuilder baseBuilder;
        private ProcessStep processStep = new ProcessStep();
        private List<Map> codeBlocks = new ArrayList<>();

        public ProcessStepBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public ProcessStep build() {
            baseBuilder.build(processStep);
            processStep.setCodeBlocks(codeBlocks);
            return processStep;
        }

        public ProcessStepBuilder isComprehensive(boolean value) {
            baseBuilder.addProperty("isComprehensive", value);
            return this;
        }

        public ProcessStepBuilder technicalPackageID(String id) {
            baseBuilder.addProperty("technicalPackageID", id);
            return this;
        }

        public ProcessStepBuilder previousProcessStep(String previousBusinessProcessId) {
            if (previousBusinessProcessId != null) {
                baseBuilder.addProperty("previousProcessStep", "/ProcessStep/" + previousBusinessProcessId);
            }
            return this;
        }

        public ProcessStepBuilder addCodeBlocks(List<Map> codeBlocks) {
            this.codeBlocks.addAll(codeBlocks);
            return this;
        }

        public CodeBlocksBuilder beginAddingCodeBlocks() {
            return new CodeBlocksBuilder(this);
        }

        public static class CodeBlocksBuilder {
            private final ProcessStepBuilder processStepBuilder;

            public CodeBlocksBuilder(ProcessStepBuilder processStepBuilder) {
                this.processStepBuilder = processStepBuilder;
            }

            public CodeBlocksBuilder codeBlock(Map map) {
                processStepBuilder.codeBlocks.add(map);
                return this;
            }

            public ProcessStepBuilder doneAddingCodeBlocks() {
                return processStepBuilder;
            }
        }
    }

    public static class CodeBlockBuilder {
        private Map<String, Object> map = new LinkedHashMap<>();

        public static CodeBlockBuilder create() {
            return new CodeBlockBuilder();
        }

        public CodeBlockBuilder codeBlockIndex(int index) {
            map.put("codeBlockIndex", index);
            return this;
        }

        public CodeBlockBuilder codeBlockTitle(String title) {
            map.put("codeBlockTitle", title);
            return this;
        }

        public CodeBlockBuilder codeBlockType(String title) {
            map.put("codeBlockType", title);
            return this;
        }

        public CodeBlockBuilder codeBlockValue(String title) {
            map.put("codeBlockValue", title);
            return this;
        }

        public CodeBlockBuilder processStepInstance(String id) {
            map.put("processStepInstance", "/ProcessStepInstance/" + id);
            return this;
        }

        public Map build() {
            return map;
        }
    }

    public static class ProcessStepInstanceBuilder {
        private final BaseBuilder baseBuilder;
        private ProcessStepInstance processStepInstance = new ProcessStepInstance();

        public ProcessStepInstanceBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public ProcessStepInstanceBuilder processExecutionCode(String code) {
            processStepInstance.setProcessExecutionCode(code);
            return this;
        }

        public ProcessStepInstanceBuilder processExecutionLog(String id) {
            processStepInstance.setProcessExecutionLog("/ProcessExecutionLog/" + id);
            return this;
        }

        public ProcessStepInstanceBuilder transformedOutputs(List<String> ids) {
            List<String> transformableOutputs = ids.stream().map(id -> "/TransformedOutput/" + id).collect(Collectors.toList());
            processStepInstance.setTransformedOutputs(transformableOutputs);
            return this;
        }

        public ProcessStepInstanceBuilder transformedInputs(List<String> ids) {
            List<String> transformableInputs = ids.stream().map(id -> "/TransformableInput/" + id).collect(Collectors.toList());
            processStepInstance.setTransformableInputs(transformableInputs);
            return this;
        }

        public ProcessStepInstance build() {
            baseBuilder.build(processStepInstance);
            return processStepInstance;
        }
    }

    public static class TransformableInputBuilder {
        private final BaseBuilder baseBuilder;
        private TransformableInput transformableInput = new TransformableInput();

        public TransformableInputBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public TransformableInputBuilder inputId(String datasetId) {
            if (datasetId != null) {
                transformableInput.setInputId("/UnitDataSet/" + datasetId);
            }
            return this;
        }

        public TransformableInput build() {
            baseBuilder.build(transformableInput);
            return transformableInput;
        }
    }

    public static class TransformedOutputBuilder {
        private final BaseBuilder baseBuilder;
        private TransformedOutput transformedOutput = new TransformedOutput();

        public TransformedOutputBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public TransformedOutputBuilder outputId(String id) {
            if (id != null) {
                transformedOutput.setOutputId("/UnitDataSet/" + id);
            }
            return this;
        }

        public TransformedOutputBuilder outputType(String outputType) {
//            transformedOutput.setOutputType(outputType);
            return this;
        }

        public TransformedOutput build() {
            baseBuilder.build(transformedOutput);
            return transformedOutput;
        }
    }

    public static class ProcessExecutionLogBuilder {
        private final BaseBuilder baseBuilder;
        private ProcessExecutionLog processExecutionLog = new ProcessExecutionLog();

        public ProcessExecutionLogBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        public ProcessExecutionLogBuilder logMessage(String message) {
            baseBuilder.addProperty("logMessage", message);
            return this;
        }

        public ProcessExecutionLog build() {
            baseBuilder.build(processExecutionLog);
            return processExecutionLog;
        }
    }


    public static List<Map<String, Object>> createListOfMap(String languageCode, String languageText) {
        return Collections.singletonList(
                Stream.of(
                        new AbstractMap.SimpleEntry<>("languageCode", languageCode),
                        new AbstractMap.SimpleEntry<>("languageText", languageText)
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
