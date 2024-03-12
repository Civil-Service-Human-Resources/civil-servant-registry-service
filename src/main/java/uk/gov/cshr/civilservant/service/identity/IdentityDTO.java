package uk.gov.cshr.civilservant.service.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityDTO {

  private String uid;
  private String username;
  private Set<String> roles = new HashSet<>();

  @JsonIgnore
  public String getEmailDomain() {
    return username.split("@")[1].toLowerCase(Locale.ROOT);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("uid", uid).toString();
  }
}
