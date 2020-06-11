package propets.accounting.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@RefreshScope
@Configuration
@Getter
@Setter
public class AccountingConfiguration {
	@Value("${secret}")
	String secret;
	@Value("${term}")
	long term;
	@Value("${template}")
	String template;
	@Value("${avatarUri}")
	String avatarUri;
}
