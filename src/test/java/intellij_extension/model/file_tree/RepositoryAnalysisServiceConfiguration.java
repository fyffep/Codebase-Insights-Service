package intellij_extension.model.file_tree;

import com.insightservice.springboot.service.RepositoryAnalysisService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RepositoryAnalysisServiceConfiguration
{
    @Bean
    public RepositoryAnalysisService repositoryAnalysisService()
    {
        return new RepositoryAnalysisService();
    }
}
