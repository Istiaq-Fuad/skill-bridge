"use client";

import React, { useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import {
  Upload,
  FileText,
  AlertCircle,
  CheckCircle,
  Download,
  Eye,
  Trash2,
  RefreshCw,
} from "lucide-react";
import { useDropzone } from "react-dropzone";
import { toast } from "sonner";
import { apiClient } from "@/lib/api";
import { useAiStore } from "@/stores/ai-store";
import type { ResumeParseResult } from "@/stores/ai-store";

export function ResumeParser() {
  const {
    resumeParses,
    currentParse,
    setResumeParseResult,
    setCurrentParse,
    updateResumeParseResult,
    clearResumeParseResult,
  } = useAiStore();

  const parseResume = useCallback(
    async (file: File) => {
      const parseId = `parse_${Date.now()}_${Math.random()
        .toString(36)
        .substr(2, 9)}`;

      const parseResult: ResumeParseResult = {
        id: parseId,
        fileName: file.name,
        status: "parsing",
      };

      setResumeParseResult(parseResult);
      setCurrentParse(parseResult);

      try {
        // Parse the resume
        const parseResponse = await apiClient.parseResume(file);

        if (parseResponse.success && parseResponse.data) {
          const updatedResult = {
            extractedData: parseResponse.data.extractedData,
            qualityScore: parseResponse.data.qualityScore,
            status: "completed" as const,
          };

          updateResumeParseResult(parseId, updatedResult);

          // Analyze resume quality
          if (parseResponse.data.extractedData) {
            const resumeText = JSON.stringify(parseResponse.data.extractedData);
            const qualityResponse = await apiClient.analyzeResumeQuality({
              resumeText,
            });

            if (qualityResponse.success && qualityResponse.data) {
              updateResumeParseResult(parseId, {
                qualityAnalysis: {
                  score: qualityResponse.data.overallScore,
                  strengths: qualityResponse.data.strengths,
                  weaknesses: qualityResponse.data.weaknesses,
                  recommendations: qualityResponse.data.improvements,
                },
              });
            }
          }

          toast.success(`Resume "${file.name}" parsed successfully!`);
        } else {
          throw new Error(parseResponse.error || "Failed to parse resume");
        }
      } catch (error) {
        console.error("Error parsing resume:", error);
        updateResumeParseResult(parseId, {
          status: "error",
          error:
            error instanceof Error ? error.message : "Failed to parse resume",
        });
        toast.error(`Failed to parse "${file.name}"`);
      }
    },
    [setResumeParseResult, setCurrentParse, updateResumeParseResult]
  );

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      for (const file of acceptedFiles) {
        await parseResume(file);
      }
    },
    [parseResume]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      "application/pdf": [".pdf"],
      "application/msword": [".doc"],
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
        [".docx"],
      "text/plain": [".txt"],
    },
    maxSize: 10 * 1024 * 1024, // 10MB
    multiple: true,
  });

  const downloadExtractedData = (parseResult: ResumeParseResult) => {
    if (!parseResult.extractedData) return;

    const data = JSON.stringify(parseResult.extractedData, null, 2);
    const blob = new Blob([data], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `extracted-data-${parseResult.fileName}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const getQualityColor = (score: number) => {
    if (score >= 80) return "text-green-600";
    if (score >= 60) return "text-yellow-600";
    return "text-red-600";
  };

  const getQualityLabel = (score: number) => {
    if (score >= 80) return "Excellent";
    if (score >= 60) return "Good";
    if (score >= 40) return "Fair";
    return "Poor";
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="text-center">
        <h1 className="text-3xl font-bold tracking-tight">AI Resume Parser</h1>
        <p className="text-muted-foreground mt-2">
          Extract and analyze resume data with AI-powered insights
        </p>
      </div>

      {/* Upload Area */}
      <Card>
        <CardContent className="p-6">
          <div
            {...getRootProps()}
            className={`
              border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors
              ${
                isDragActive
                  ? "border-primary bg-primary/5"
                  : "border-muted-foreground/25 hover:border-primary/50"
              }
            `}
          >
            <input {...getInputProps()} />
            <Upload className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
            <h3 className="text-lg font-medium mb-2">
              {isDragActive ? "Drop files here" : "Upload Resume Files"}
            </h3>
            <p className="text-muted-foreground mb-4">
              Drag & drop resume files here, or click to browse
            </p>
            <p className="text-sm text-muted-foreground">
              Supports PDF, DOC, DOCX, TXT files up to 10MB
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Parsed Resumes */}
      {resumeParses.length > 0 && (
        <div className="space-y-4">
          <h2 className="text-2xl font-bold">Parsed Resumes</h2>

          <div className="grid gap-4">
            {resumeParses.map((parseResult) => (
              <Card key={parseResult.id} className="overflow-hidden">
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <FileText className="h-5 w-5 text-muted-foreground" />
                      <div>
                        <CardTitle className="text-base">
                          {parseResult.fileName}
                        </CardTitle>
                        <div className="flex items-center gap-2 mt-1">
                          {parseResult.status === "parsing" && (
                            <Badge variant="outline" className="text-blue-600">
                              <RefreshCw className="h-3 w-3 mr-1 animate-spin" />
                              Parsing...
                            </Badge>
                          )}
                          {parseResult.status === "completed" && (
                            <Badge variant="outline" className="text-green-600">
                              <CheckCircle className="h-3 w-3 mr-1" />
                              Completed
                            </Badge>
                          )}
                          {parseResult.status === "error" && (
                            <Badge variant="outline" className="text-red-600">
                              <AlertCircle className="h-3 w-3 mr-1" />
                              Error
                            </Badge>
                          )}

                          {parseResult.qualityScore && (
                            <Badge
                              variant="secondary"
                              className={getQualityColor(
                                parseResult.qualityScore
                              )}
                            >
                              {getQualityLabel(parseResult.qualityScore)}(
                              {parseResult.qualityScore}%)
                            </Badge>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      {parseResult.status === "completed" && (
                        <>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setCurrentParse(parseResult)}
                          >
                            <Eye className="h-4 w-4 mr-2" />
                            View
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => downloadExtractedData(parseResult)}
                          >
                            <Download className="h-4 w-4 mr-2" />
                            Download
                          </Button>
                        </>
                      )}
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => clearResumeParseResult(parseResult.id)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardHeader>

                {parseResult.status === "parsing" && (
                  <CardContent className="pt-0">
                    <Progress value={65} className="w-full" />
                  </CardContent>
                )}

                {parseResult.status === "error" && parseResult.error && (
                  <CardContent className="pt-0">
                    <div className="text-sm text-red-600 bg-red-50 p-3 rounded-md">
                      {parseResult.error}
                    </div>
                  </CardContent>
                )}
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* Resume Details View */}
      {currentParse &&
        currentParse.status === "completed" &&
        currentParse.extractedData && (
          <Card>
            <CardHeader>
              <CardTitle>Resume Analysis: {currentParse.fileName}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Contact Information */}
              {currentParse.extractedData.contactInfo && (
                <div>
                  <h3 className="font-medium mb-3">Contact Information</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="font-medium">Name:</span>{" "}
                      {currentParse.extractedData.contactInfo.name}
                    </div>
                    <div>
                      <span className="font-medium">Email:</span>{" "}
                      {currentParse.extractedData.contactInfo.email}
                    </div>
                    <div>
                      <span className="font-medium">Phone:</span>{" "}
                      {currentParse.extractedData.contactInfo.phone}
                    </div>
                    <div>
                      <span className="font-medium">Location:</span>{" "}
                      {currentParse.extractedData.contactInfo.address}
                    </div>
                    {currentParse.extractedData.contactInfo.linkedin && (
                      <div>
                        <span className="font-medium">LinkedIn:</span>{" "}
                        {currentParse.extractedData.contactInfo.linkedin}
                      </div>
                    )}
                    {currentParse.extractedData.contactInfo.github && (
                      <div>
                        <span className="font-medium">GitHub:</span>{" "}
                        {currentParse.extractedData.contactInfo.github}
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Skills */}
              {currentParse.extractedData.skills &&
                currentParse.extractedData.skills.length > 0 && (
                  <div>
                    <h3 className="font-medium mb-3">Skills</h3>
                    <div className="flex flex-wrap gap-2">
                      {currentParse.extractedData.skills.map((skill, index) => (
                        <Badge key={index} variant="secondary">
                          {skill}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}

              {/* Experience */}
              {currentParse.extractedData.experience &&
                currentParse.extractedData.experience.length > 0 && (
                  <div>
                    <h3 className="font-medium mb-3">Experience</h3>
                    <div className="space-y-4">
                      {currentParse.extractedData.experience.map(
                        (exp, index) => (
                          <div
                            key={index}
                            className="border-l-2 border-primary pl-4"
                          >
                            <h4 className="font-medium">{exp.position}</h4>
                            <p className="text-sm text-muted-foreground">
                              {exp.company}
                            </p>
                            <p className="text-sm text-muted-foreground">
                              {exp.startDate} - {exp.endDate || "Present"}
                            </p>
                            <p className="text-sm mt-2">{exp.description}</p>
                            {exp.skills && exp.skills.length > 0 && (
                              <div className="flex flex-wrap gap-1 mt-2">
                                {exp.skills.map((skill, skillIndex) => (
                                  <Badge
                                    key={skillIndex}
                                    variant="outline"
                                    className="text-xs"
                                  >
                                    {skill}
                                  </Badge>
                                ))}
                              </div>
                            )}
                          </div>
                        )
                      )}
                    </div>
                  </div>
                )}

              {/* Education */}
              {currentParse.extractedData.education &&
                currentParse.extractedData.education.length > 0 && (
                  <div>
                    <h3 className="font-medium mb-3">Education</h3>
                    <div className="space-y-3">
                      {currentParse.extractedData.education.map(
                        (edu, index) => (
                          <div
                            key={index}
                            className="border-l-2 border-secondary pl-4"
                          >
                            <h4 className="font-medium">
                              {edu.degree} in {edu.field}
                            </h4>
                            <p className="text-sm text-muted-foreground">
                              {edu.institution}
                            </p>
                            <p className="text-sm text-muted-foreground">
                              {edu.startDate} - {edu.endDate || "Present"}
                            </p>
                            {edu.gpa && (
                              <p className="text-sm">GPA: {edu.gpa}</p>
                            )}
                          </div>
                        )
                      )}
                    </div>
                  </div>
                )}

              {/* Quality Analysis */}
              {currentParse.qualityAnalysis && (
                <div>
                  <h3 className="font-medium mb-3">Resume Quality Analysis</h3>
                  <div className="space-y-4">
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-medium">
                        Overall Score:
                      </span>
                      <Badge
                        variant="secondary"
                        className={getQualityColor(
                          currentParse.qualityAnalysis.score
                        )}
                      >
                        {currentParse.qualityAnalysis.score}% -{" "}
                        {getQualityLabel(currentParse.qualityAnalysis.score)}
                      </Badge>
                    </div>

                    {currentParse.qualityAnalysis.strengths.length > 0 && (
                      <div>
                        <h4 className="text-sm font-medium text-green-600 mb-2">
                          Strengths:
                        </h4>
                        <ul className="text-sm space-y-1">
                          {currentParse.qualityAnalysis.strengths.map(
                            (strength, index) => (
                              <li
                                key={index}
                                className="flex items-start gap-2"
                              >
                                <CheckCircle className="h-4 w-4 text-green-600 mt-0.5 flex-shrink-0" />
                                {strength}
                              </li>
                            )
                          )}
                        </ul>
                      </div>
                    )}

                    {currentParse.qualityAnalysis.weaknesses.length > 0 && (
                      <div>
                        <h4 className="text-sm font-medium text-red-600 mb-2">
                          Areas for Improvement:
                        </h4>
                        <ul className="text-sm space-y-1">
                          {currentParse.qualityAnalysis.weaknesses.map(
                            (weakness, index) => (
                              <li
                                key={index}
                                className="flex items-start gap-2"
                              >
                                <AlertCircle className="h-4 w-4 text-red-600 mt-0.5 flex-shrink-0" />
                                {weakness}
                              </li>
                            )
                          )}
                        </ul>
                      </div>
                    )}

                    {currentParse.qualityAnalysis.recommendations.length >
                      0 && (
                      <div>
                        <h4 className="text-sm font-medium text-blue-600 mb-2">
                          Recommendations:
                        </h4>
                        <ul className="text-sm space-y-1">
                          {currentParse.qualityAnalysis.recommendations.map(
                            (rec, index) => (
                              <li
                                key={index}
                                className="flex items-start gap-2"
                              >
                                <AlertCircle className="h-4 w-4 text-blue-600 mt-0.5 flex-shrink-0" />
                                {rec}
                              </li>
                            )
                          )}
                        </ul>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        )}
    </div>
  );
}
