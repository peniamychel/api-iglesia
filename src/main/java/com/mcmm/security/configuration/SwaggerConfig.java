package com.mcmm.security.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Iglesia")
                        .version("1.0.0")
                        .description("Documentación de la API para la aplicación de gestión de iglesia"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // Agregamos el endpoint de login manualmente a los paths de OpenAPI
                .paths(new Paths().addPathItem("/login", new PathItem()
                        .post(new Operation()
                                .summary("Iniciar sesión")
                                .description("Endpoint para autenticarse y obtener el token JWT")
                                .requestBody(new RequestBody()
                                        .required(true)
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema<>()
                                                        .type("object")
                                                        .addProperty("username", new Schema<>().type("string").example("admin"))
                                                        .addProperty("password", new Schema<>().type("string").example("123456"))
                                                ))))
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse()
                                                .description("Autenticación Exitosa")
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType().schema(new Schema<>()
                                                                .type("object")
                                                                .addProperty("success", new Schema<>().type("boolean").example(true))
                                                                .addProperty("message", new Schema<>().type("string").example("Autenticaion Exitosa"))
                                                                .addProperty("token", new Schema<>().type("string").example("eyJhbGciOi..."))
                                                                .addProperty("refreshToken", new Schema<>().type("string").example("eyJhbGciOi..."))
                                                                .addProperty("username", new Schema<>().type("string").example("admin"))
                                                        ))))
                                ))));
    }

    @Bean
    public SwaggerIndexPageTransformer swaggerIndexPageTransformer(
            SwaggerUiConfigProperties swaggerUiConfigProperties,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerUiConfigParameters swaggerUiConfigParameters,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {
        
        return new SwaggerIndexPageTransformer(
                swaggerUiConfigProperties,
                swaggerUiOAuthProperties,
                swaggerUiConfigParameters,
                swaggerWelcomeCommon,
                objectMapperProvider) {
            
            @Override
            public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformer) throws IOException {
                Resource transformedResource = super.transform(request, resource, transformer);
                
                if (resource.toString().contains("index.html")) {
                    String html = StreamUtils.copyToString(transformedResource.getInputStream(), StandardCharsets.UTF_8);
                    
                    // JS personalizado inyectado al final del body para interceptar el fetch de /login y auto-autorizar
                    String customJs = 
                            "<script>\n" +
                            "  (function() {\n" +
                            "    console.log('Automated Swagger Auth active');\n" +
                            "    const originalFetch = window.fetch;\n" +
                            "    window.fetch = async function(...args) {\n" +
                            "      const response = await originalFetch(...args);\n" +
                            "      const url = args[0];\n" +
                            "      if (typeof url === 'string' && url.endsWith('/login')) {\n" +
                            "        const clone = response.clone();\n" +
                            "        try {\n" +
                            "          const data = await clone.json();\n" +
                            "          if (data && data.token) {\n" +
                            "            const token = data.token;\n" +
                            "            if (window.ui) {\n" +
                            "              window.ui.preauthorizeApiKey('Bearer Authentication', token);\n" +
                            "              localStorage.setItem('swagger_token', token);\n" +
                            "              console.log('JWT Token auto-authorized in Swagger UI.');\n" +
                            "            }\n" +
                            "          }\n" +
                            "        } catch (e) {\n" +
                            "          console.error('Error auto-authorizing Swagger', e);\n" +
                            "        }\n" +
                            "      }\n" +
                            "      return response;\n" +
                            "    };\n" +
                            "    \n" +
                            "    // Restaurar el token de localStorage al recargar la página\n" +
                            "    window.addEventListener('load', () => {\n" +
                            "      const checkUi = setInterval(() => {\n" +
                            "        if (window.ui) {\n" +
                            "          clearInterval(checkUi);\n" +
                            "          const storedToken = localStorage.getItem('swagger_token');\n" +
                            "          if (storedToken) {\n" +
                            "            window.ui.preauthorizeApiKey('Bearer Authentication', storedToken);\n" +
                            "            console.log('JWT Token restored from localStorage.');\n" +
                            "          }\n" +
                            "        }\n" +
                            "      }, 100);\n" +
                            "      setTimeout(() => clearInterval(checkUi), 10000);\n" +
                            "    });\n" +
                            "  })();\n" +
                            "</script>";
                    
                    html = html.replace("</body>", customJs + "</body>");
                    return new TransformedResource(resource, html.getBytes(StandardCharsets.UTF_8));
                }
                
                return transformedResource;
            }
        };
    }
}