/*
 * Copyright 2020 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package azkaban.imagemgmt.version;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class VersionSetBuilderTest {

  @Test
  public void testVersionSet() throws IOException {
    final String EXPECTED_JSON = "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}";
    final String EXPECTED_MD5 = "43966138aebfdc4438520cc5cd2aefa8";

    // Test if the elements are ordered by keys, not by order of addition
    VersionSetLoader versionSetLoader = Mockito.mock(VersionSetLoader.class);
    Mockito.when(versionSetLoader.getVersionSet(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(java.util.Optional.of(new VersionSet(EXPECTED_JSON, EXPECTED_MD5, 1)));
    VersionSet versionSet = new VersionSetBuilder(versionSetLoader)
        .addElement("key1", "value1")
        .addElement("key3", "value3")
        .addElement("key2", "value2")
        .build();
    Assert.assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}",
        versionSet.getVersionSetJsonString());
    Assert.assertEquals("43966138aebfdc4438520cc5cd2aefa8", versionSet.getVersionSetMd5Hex());
    Assert.assertEquals(1, versionSet.getVersionSetId());
    Assert.assertEquals(versionSet.getVersion("key3").get(), "value3");
    Assert.assertEquals(versionSet.getVersion("key1").get(), "value1");
    Assert.assertEquals(versionSet.getVersion("key2").get(), "value2");
    Assert.assertEquals(versionSet.getVersion("key4").isPresent(), false);
  }
}