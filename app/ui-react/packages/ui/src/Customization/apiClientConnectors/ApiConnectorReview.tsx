import {
  Card,
  CardBody,
  CardTitle,
  PageSection,
  Text,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  TextVariants,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import './ApiConnectorReview.css';

export interface IApiConnectorReviewProps {
  apiConnectorDescription?: string;
  apiConnectorName?: string;
  errorMessages?: string[];
  i18nApiDefinitionHeading: string;
  i18nDescriptionLabel: string;
  i18nErrorsHeading?: string;
  i18nImportedHeading: string;
  i18nNameLabel: string;
  i18nOperationsHtmlMessage: string;
  i18nOperationTagHtmlMessages?: string[];
  i18nTitle: string;
  i18nValidationFallbackMessage?: string;
  i18nWarningsHeading?: string;
  warningMessages?: string[];
}

export class ApiConnectorReview extends React.Component<IApiConnectorReviewProps> {
  public render() {
    return (
      <PageSection>
        <Card>
          <CardTitle>
            <Title size="lg" headingLevel={'h2'}>
              {this.props.i18nTitle}
            </Title>
          </CardTitle>
          <CardBody>
            {this.props.i18nValidationFallbackMessage ? (
              <h5 className="api-connector-review__validationFallbackMessage">
                {this.props.i18nValidationFallbackMessage}
              </h5>
            ) : (
              <TextContent>
                <Title
                  headingLevel="h5"
                  size="md"
                  className="customization-details__heading"
                >
                  {this.props.i18nApiDefinitionHeading}
                </Title>
                <TextList component={TextListVariants.dl}>
                  <TextListItem component={TextListItemVariants.dt}>
                    {this.props.i18nNameLabel}
                  </TextListItem>
                  <TextListItem component={TextListItemVariants.dd}>
                    {this.props.apiConnectorName}
                  </TextListItem>
                  <TextListItem component={TextListItemVariants.dt}>
                    {this.props.i18nDescriptionLabel}
                  </TextListItem>
                  <TextListItem component={TextListItemVariants.dd}>
                    {this.props.apiConnectorDescription}
                  </TextListItem>
                </TextList>
                <Title
                  headingLevel="h5"
                  size="md"
                  className="customization-details__heading"
                >
                  {this.props.i18nImportedHeading}
                </Title>
                <Text
                  component={TextVariants.p}
                  dangerouslySetInnerHTML={{
                    __html: this.props.i18nOperationsHtmlMessage,
                  }}
                />

                {/* tagged messages */}
                {this.props.i18nOperationTagHtmlMessages && (
                  <TextList className="api-connector-review__tagMessageList">
                    {this.props.i18nOperationTagHtmlMessages.map(
                      (msg: string, index: number) => (
                        <TextListItem
                          key={index}
                          dangerouslySetInnerHTML={{ __html: msg }}
                        />
                      )
                    )}
                  </TextList>
                )}

                {/* error messages */}
                {this.props.i18nErrorsHeading && this.props.errorMessages && (
                  <Title
                    headingLevel="h5"
                    size="md"
                    className="customization-details__heading"
                  >
                    {this.props.i18nErrorsHeading}
                  </Title>
                )}
                {this.props.errorMessages
                  ? this.props.errorMessages.map(
                      (errorMsg: string, index: number) => (
                        <Text component={TextVariants.p} key={index}>
                          {index + 1}. {errorMsg}
                        </Text>
                      )
                    )
                  : null}

                {/* warning messages */}
                {this.props.i18nWarningsHeading && this.props.warningMessages && (
                  <Title
                    headingLevel="h5"
                    size="md"
                    className="customization-details__heading"
                  >
                    {this.props.i18nWarningsHeading}
                  </Title>
                )}
                {this.props.warningMessages
                  ? this.props.warningMessages.map(
                      (warningMsg: string, index: number) => (
                        <Text key={index} component={TextVariants.p}>
                          {index + 1}. {warningMsg}
                        </Text>
                      )
                    )
                  : null}
              </TextContent>
            )}
          </CardBody>
        </Card>
      </PageSection>
    );
  }
}
