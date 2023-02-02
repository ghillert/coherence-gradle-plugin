/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package petstore;

import com.tangosol.io.pof.schema.annotation.Portable;
import com.tangosol.io.pof.schema.annotation.PortableType;

@PortableType(id = 2, version = 1)
public class Dog extends Pet
{

		@Portable
		private String breed;

		@Portable(since = 1)
		private Color color;


	public Dog()
	{
	}

	public String getBreed() {
		return breed;
	}

	public void setBreed(String breed) {
		this.breed = breed;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
