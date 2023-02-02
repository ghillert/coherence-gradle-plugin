/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package petstore;

import com.tangosol.io.pof.schema.annotation.Portable;
import com.tangosol.io.pof.schema.annotation.PortableType;

/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
@PortableType(id = 1, version = 1)
public class Pet
	{
	@Portable
	protected String name;

	@Portable(since = 1)
	protected int age;

	public Pet()
	{
	}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}
