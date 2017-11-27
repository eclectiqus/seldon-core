package io.seldon.engine.metrics;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.micrometer.core.instrument.Tag;
import io.micrometer.spring.web.client.RestTemplateExchangeTags;
import io.micrometer.spring.web.client.RestTemplateExchangeTagsProvider;
import io.seldon.engine.predictors.EnginePredictor;
import io.seldon.engine.predictors.PredictiveUnitState;
import io.seldon.engine.service.InternalPredictionService;

@Component
public class SeldonRestTemplateExchangeTagsProvider implements RestTemplateExchangeTagsProvider {

	private final static String PROJECT_ANNOTATION_KEY = "project_name";
	private final static String PREDICTOR_NAME_METRIC = "predictor_name";
	private final static String PREDICTOR_VERSION_METRIC = "predictor_version";
	private final static String MODEL_NAME_METRIC = "model_name";
	private final static String MODEL_IMAGE_METRIC = "model_image";
	private final static String MODEL_VERSION_METRIC = "model_version";
	
	@Autowired
	EnginePredictor enginePredictor;
	
	@Override
	public Iterable<Tag> getTags(String urlTemplate, HttpRequest request, ClientHttpResponse response) 
	{
		Tag uriTag = StringUtils.hasText(urlTemplate)? RestTemplateExchangeTags.uri(urlTemplate): RestTemplateExchangeTags.uri(request);
		
		
	            
		return Arrays.asList(RestTemplateExchangeTags.method(request), uriTag,
				RestTemplateExchangeTags.status(response),
	            RestTemplateExchangeTags.clientName(request),
	            modelName(request),
	            modelImage(request),
	            modelVersion(request),
	            projectName(),
	            predictorName(),
	            predictorVersion());
	}
	
	public Iterable<Tag> getModelMetrics(PredictiveUnitState state)
	{
		return Arrays.asList(
				 projectName(),
				 predictorName(),
				 predictorVersion(),
				 modelName(state.name),
				 modelImage(state.imageName),
				 modelVersion(state.imageVersion));
	}
	
	public Tag projectName()
	{
		return Tag.of("project_name",enginePredictor.getPredictorSpec().getAnnotationsOrDefault(PROJECT_ANNOTATION_KEY, "unknown"));
	}
	
	
	private Tag predictorName()
	{
		if (!StringUtils.hasText(enginePredictor.getPredictorSpec().getName()))
			return Tag.of(PREDICTOR_NAME_METRIC, "unknown");
		else
			return Tag.of(PREDICTOR_NAME_METRIC,enginePredictor.getPredictorSpec().getName()); 
	}
	
	private Tag predictorVersion()
	{
		if (!StringUtils.hasText(enginePredictor.getPredictorSpec().getVersion()))
			return Tag.of(PREDICTOR_VERSION_METRIC, "unknown");
		else
			return Tag.of(PREDICTOR_VERSION_METRIC, enginePredictor.getPredictorSpec().getVersion());
	}

	private Tag modelName(HttpRequest request)
	{
		String modelName = request.getHeaders().getFirst(InternalPredictionService.MODEL_NAME_HEADER);
		return modelName(modelName);
	}
	
	public Tag modelName(String modelName)
	{
		if (!StringUtils.hasText(modelName))
			modelName = "unknown";
		return Tag.of(MODEL_NAME_METRIC, modelName);
	}
	
	private Tag modelImage(HttpRequest request)
	{
		String modelImage = request.getHeaders().getFirst(InternalPredictionService.MODEL_IMAGE_HEADER);
		return modelImage(modelImage);
	}
	
	private Tag modelImage(String modelImage)
	{
		if (!StringUtils.hasText(modelImage))
			modelImage = "unknown";
		
		return Tag.of(MODEL_IMAGE_METRIC, modelImage);
	}

	private Tag modelVersion(HttpRequest request)
	{
		String modelVersion = request.getHeaders().getFirst(InternalPredictionService.MODEL_VERSION_HEADER);
		return modelVersion(modelVersion);
	}
	
	public Tag modelVersion(String modelVersion)
	{
		if (!StringUtils.hasText(modelVersion))
			modelVersion = "latest";
		
		return Tag.of(MODEL_VERSION_METRIC, modelVersion);
	}
	
	

}
