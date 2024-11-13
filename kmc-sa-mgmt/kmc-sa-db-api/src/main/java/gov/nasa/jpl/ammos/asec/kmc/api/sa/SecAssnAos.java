package gov.nasa.jpl.ammos.asec.kmc.api.sa;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "SecAssnAos")
@Table(name = "security_associations_aos")
public class SecAssnAos extends SecAssn {
}
