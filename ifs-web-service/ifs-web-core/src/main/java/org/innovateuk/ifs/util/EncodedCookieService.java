package org.innovateuk.ifs.util;

import org.springframework.stereotype.Service;

@Service
public class EncodedCookieService extends AbstractCookieService {

    @Override
    protected String getValueToSave(String value) {
        return encodeValue(value);
    }

    @Override
    protected String getValueFromCookie(String value) {
        return decodeValue(value);
    }
}
